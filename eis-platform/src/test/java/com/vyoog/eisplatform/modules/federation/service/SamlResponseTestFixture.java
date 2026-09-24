package com.vyoog.eisplatform.modules.federation.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Test-only: builds a REAL, cryptographically-signed SAML Response XML using
 * the actual RSA key material checked into {@code src/test/resources/saml/}
 * (a throwaway, openssl-generated self-signed test certificate/key — never a
 * real credential), signed via the JDK's own built-in {@code javax.xml.crypto.dsig}
 * XML Signature API, exactly the shape a real IdP would produce. Deliberately
 * NOT using java-saml-core to build this (that library is the SP/consumer
 * side only — it has no Response-building API), so this is the one place in
 * the Phase 5 test suite that constructs raw SAML XML directly, precisely so
 * every OTHER test can validate the real parsing/signature-verification code
 * in {@link SamlAuthenticationService} against a genuinely signed assertion,
 * not a fabricated stand-in.
 */
final class SamlResponseTestFixture {

    private SamlResponseTestFixture() {
    }

    static PrivateKey loadPrivateKey() throws Exception {
        String pem = readClasspathText("/saml/idp-test.key");
        String base64 = pem.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(base64);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    static X509Certificate loadCertificate() throws Exception {
        String pem = readClasspathText("/saml/idp-test.crt");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(
            new java.io.ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8)));
    }

    static String certificatePem() throws Exception {
        return readClasspathText("/saml/idp-test.crt");
    }

    private static String isoSeconds(Instant instant) {
        return DateTimeFormatter.ISO_INSTANT.format(instant.truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
    }

    private static String readClasspathText(String path) throws Exception {
        try (var in = SamlResponseTestFixture.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing test resource: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static final class Params {
        String responseId = "_response-" + System.nanoTime();
        String assertionId = "_assertion-" + System.nanoTime();
        String inResponseTo;
        String destination;
        String issuer;
        String nameId;
        String nameIdFormat = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
        String audience;
        Instant issueInstant = Instant.now();
        Instant notBefore = Instant.now().minusSeconds(60);
        Instant notOnOrAfter = Instant.now().plusSeconds(300);
        Instant confirmationNotOnOrAfter = Instant.now().plusSeconds(300);
        Map<String, String> attributes = Map.of();
        boolean sign = true;
        boolean tamperAfterSigning = false;

        Params withInResponseTo(String v) { this.inResponseTo = v; return this; }
        Params withDestination(String v) { this.destination = v; return this; }
        Params withIssuer(String v) { this.issuer = v; return this; }
        Params withNameId(String v) { this.nameId = v; return this; }
        Params withAudience(String v) { this.audience = v; return this; }
        Params withAttributes(Map<String, String> v) { this.attributes = v; return this; }
        Params withNotOnOrAfter(Instant v) { this.notOnOrAfter = v; this.confirmationNotOnOrAfter = v; return this; }
        Params withoutSignature() { this.sign = false; return this; }
        Params withTamperAfterSigning() { this.tamperAfterSigning = true; return this; }
    }

    /** Returns the raw base64 string a real HTTP-POST binding form field
     * ({@code SAMLResponse}) would contain. */
    static String buildEncodedResponse(Params p) throws Exception {
        String fmt = isoSeconds(p.issueInstant);
        StringBuilder attrStatement = new StringBuilder();
        if (!p.attributes.isEmpty()) {
            attrStatement.append("<saml:AttributeStatement>");
            for (var entry : p.attributes.entrySet()) {
                attrStatement.append("<saml:Attribute Name=\"").append(entry.getKey()).append("\">")
                    .append("<saml:AttributeValue>").append(entry.getValue()).append("</saml:AttributeValue>")
                    .append("</saml:Attribute>");
            }
            attrStatement.append("</saml:AttributeStatement>");
        }

        String xml = "<samlp:Response xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" "
            + "xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\" "
            + "ID=\"" + p.responseId + "\" Version=\"2.0\" IssueInstant=\"" + fmt + "\" "
            + "Destination=\"" + p.destination + "\" InResponseTo=\"" + p.inResponseTo + "\">"
            + "<saml:Issuer>" + p.issuer + "</saml:Issuer>"
            + "<samlp:Status><samlp:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/></samlp:Status>"
            + "<saml:Assertion ID=\"" + p.assertionId + "\" Version=\"2.0\" IssueInstant=\"" + fmt + "\">"
            + "<saml:Issuer>" + p.issuer + "</saml:Issuer>"
            + "<saml:Subject>"
            + "<saml:NameID Format=\"" + p.nameIdFormat + "\">" + p.nameId + "</saml:NameID>"
            + "<saml:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\">"
            + "<saml:SubjectConfirmationData InResponseTo=\"" + p.inResponseTo + "\" "
            + "NotOnOrAfter=\"" + isoSeconds(p.confirmationNotOnOrAfter) + "\" "
            + "Recipient=\"" + p.destination + "\"/>"
            + "</saml:SubjectConfirmation>"
            + "</saml:Subject>"
            + "<saml:Conditions NotBefore=\"" + isoSeconds(p.notBefore) + "\" "
            + "NotOnOrAfter=\"" + isoSeconds(p.notOnOrAfter) + "\">"
            + "<saml:AudienceRestriction><saml:Audience>" + p.audience + "</saml:Audience></saml:AudienceRestriction>"
            + "</saml:Conditions>"
            + "<saml:AuthnStatement AuthnInstant=\"" + fmt + "\" SessionIndex=\"_session-" + System.nanoTime() + "\">"
            + "<saml:AuthnContext><saml:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport</saml:AuthnContextClassRef></saml:AuthnContext>"
            + "</saml:AuthnStatement>"
            + attrStatement
            + "</saml:Assertion>"
            + "</samlp:Response>";

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document document = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

        Element assertionElement = (Element) document.getElementsByTagNameNS(
            "urn:oasis:names:tc:SAML:2.0:assertion", "Assertion").item(0);
        assertionElement.setIdAttribute("ID", true);

        if (p.sign) {
            signAssertion(document, assertionElement, p.assertionId);
        }

        if (p.tamperAfterSigning) {
            // Simulates an attacker modifying a signed assertion in transit —
            // change the NameID's text content AFTER signing, so the digest
            // computed at signing time no longer matches. Used to prove
            // signature verification actually re-checks integrity, not just
            // "a signature element is present".
            NodeList nameIds = document.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "NameID");
            nameIds.item(0).setTextContent("attacker@evil.example.com");
        }

        String signedXml = serialize(document);
        return Base64.getEncoder().encodeToString(signedXml.getBytes(StandardCharsets.UTF_8));
    }

    private static void signAssertion(Document document, Element assertionElement, String assertionId) throws Exception {
        PrivateKey privateKey = loadPrivateKey();
        X509Certificate certificate = loadCertificate();

        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        Reference ref = fac.newReference("#" + assertionId,
            fac.newDigestMethod(DigestMethod.SHA256, null),
            List.of(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null),
                fac.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null)),
            null, null);
        SignedInfo signedInfo = fac.newSignedInfo(
            fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
            fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
            List.of(ref));

        KeyInfoFactory kif = fac.getKeyInfoFactory();
        X509Data x509Data = kif.newX509Data(List.of(certificate));
        KeyInfo keyInfo = kif.newKeyInfo(List.of(x509Data));

        // Per the SAML XML Signature profile, ds:Signature must be the child
        // immediately following saml:Issuer (i.e. right before saml:Subject).
        Node issuerNode = assertionElement.getElementsByTagNameNS(
            "urn:oasis:names:tc:SAML:2.0:assertion", "Issuer").item(0);
        Node nextSibling = issuerNode.getNextSibling();

        DOMSignContext signContext = new DOMSignContext(privateKey, assertionElement, nextSibling);
        XMLSignature signature = fac.newXMLSignature(signedInfo, keyInfo);
        signature.sign(signContext);
    }

    private static String serialize(Document document) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }
}
