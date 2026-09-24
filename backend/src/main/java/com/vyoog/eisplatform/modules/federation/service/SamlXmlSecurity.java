package com.vyoog.eisplatform.modules.federation.service;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import org.xml.sax.InputSource;

/**
 * Phase 4 (2026.3.3): an explicit, auditable XXE defense applied to every
 * piece of IdP-supplied XML BEFORE it reaches the SAML library — belt and
 * braces on top of whatever the library's own parser already does
 * internally, since this admin-facing feature accepts XML pasted/uploaded
 * by an organization admin (who could themselves be compromised, or simply
 * copy-paste something malicious without realizing it) and that XML
 * originates from a third party (the customer's own IdP), not from this
 * platform itself.
 *
 * <p>Uses the standard hardening recipe (OWASP's own XXE Prevention Cheat
 * Sheet): disallow any DOCTYPE declaration outright (a metadata document
 * has no legitimate reason to declare one), disable external general and
 * parameter entities, disable external DTD loading, disable XInclude, and
 * enable {@link XMLConstants#FEATURE_SECURE_PROCESSING} — this alone is
 * sufficient to make classic XXE (local file read, SSRF via external
 * entity, billion-laughs) impossible against this parser, independent of
 * whatever the downstream SAML library does with the same string.
 */
final class SamlXmlSecurity {

    private SamlXmlSecurity() {
    }

    /** Throws if the given XML is not well-formed, or declares a DOCTYPE at
     * all — never attempts to "sanitize" a DOCTYPE out and continue, since
     * silently rewriting untrusted input is its own source of bugs. Callers
     * still parse the ORIGINAL string with the SAML library afterward; this
     * is a validation gate, not a transformation. */
    static void rejectUnsafeXml(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException("Metadata XML is required");
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            // The JDK's default ErrorHandler prints fatal errors straight to
            // stderr in addition to throwing — this is an EXPECTED rejection
            // path (any DOCTYPE, by design), not an unhandled fault, so it's
            // handled here instead of left to look like an uncaught crash in
            // the logs.
            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) {
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }
            });
            builder.parse(new InputSource(new StringReader(xml)));
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Could not configure a secure XML parser", e);
        } catch (SAXException e) {
            // Includes the "DOCTYPE is disallowed" case — disallow-doctype-decl
            // makes the parser throw a SAXParseException for any DOCTYPE, which
            // is exactly the outcome wanted, just surfaced as a normal
            // validation error rather than a distinct exception type.
            throw new IllegalArgumentException("This XML is not valid, or declares a DOCTYPE (not permitted for security reasons): " + e.getMessage());
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the provided XML: " + e.getMessage());
        }
    }
}
