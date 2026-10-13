package com.vyoog.eisplatform.modules.billing.tax.support;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Reference data (not tax rates): the states and union territories of countries whose tax depends on the state.
 * {@link #normalize} turns what a customer typed (a name, a code, India's two-digit GST code) into one key, so a rule's
 * state and an address's state compare equal. Other countries compare the typed text after removing case, accents and
 * punctuation. */
public final class StateCatalog {

    public record State(String code, String name, String gstCode) {
    }

    private static final Map<String, List<State>> STATES = new LinkedHashMap<>();
    private static final Map<String, Map<String, State>> INDEX = new LinkedHashMap<>();
    private static final Map<String, String> ALIASES_IN = Map.ofEntries(
        Map.entry("ORISSA", "OD"), Map.entry("PONDICHERRY", "PY"), Map.entry("UTTARANCHAL", "UK"),
        Map.entry("NEWDELHI", "DL"), Map.entry("DELHINCT", "DL"), Map.entry("NCTOFDELHI", "DL"), Map.entry("NATIONALCAPITALTERRITORYOFDELHI", "DL"),
        Map.entry("TELENGANA", "TG"), Map.entry("CHATTISGARH", "CT"), Map.entry("BANGALORE", "KA"), Map.entry("DADRAANDNAGARHAVELI", "DH"),
        Map.entry("DAMANANDDIU", "DH"), Map.entry("DAMANDIU", "DH"), Map.entry("JAMMUKASHMIR", "JK"), Map.entry("ANDAMANNICOBAR", "AN"));

    static {
        add("IN", "AN:Andaman and Nicobar Islands:35", "AP:Andhra Pradesh:37", "AR:Arunachal Pradesh:12", "AS:Assam:18", "BR:Bihar:10",
            "CH:Chandigarh:04", "CT:Chhattisgarh:22", "DH:Dadra and Nagar Haveli and Daman and Diu:26", "DL:Delhi:07", "GA:Goa:30",
            "GJ:Gujarat:24", "HR:Haryana:06", "HP:Himachal Pradesh:02", "JK:Jammu and Kashmir:01", "JH:Jharkhand:20", "KA:Karnataka:29",
            "KL:Kerala:32", "LA:Ladakh:38", "LD:Lakshadweep:31", "MP:Madhya Pradesh:23", "MH:Maharashtra:27", "MN:Manipur:14",
            "ML:Meghalaya:17", "MZ:Mizoram:15", "NL:Nagaland:13", "OD:Odisha:21", "PY:Puducherry:34", "PB:Punjab:03", "RJ:Rajasthan:08",
            "SK:Sikkim:11", "TN:Tamil Nadu:33", "TG:Telangana:36", "TR:Tripura:16", "UP:Uttar Pradesh:09", "UK:Uttarakhand:05", "WB:West Bengal:19");
        add("US", "AL:Alabama", "AK:Alaska", "AZ:Arizona", "AR:Arkansas", "CA:California", "CO:Colorado", "CT:Connecticut", "DE:Delaware",
            "DC:District of Columbia", "FL:Florida", "GA:Georgia", "HI:Hawaii", "ID:Idaho", "IL:Illinois", "IN:Indiana", "IA:Iowa", "KS:Kansas",
            "KY:Kentucky", "LA:Louisiana", "ME:Maine", "MD:Maryland", "MA:Massachusetts", "MI:Michigan", "MN:Minnesota", "MS:Mississippi",
            "MO:Missouri", "MT:Montana", "NE:Nebraska", "NV:Nevada", "NH:New Hampshire", "NJ:New Jersey", "NM:New Mexico", "NY:New York",
            "NC:North Carolina", "ND:North Dakota", "OH:Ohio", "OK:Oklahoma", "OR:Oregon", "PA:Pennsylvania", "RI:Rhode Island",
            "SC:South Carolina", "SD:South Dakota", "TN:Tennessee", "TX:Texas", "UT:Utah", "VT:Vermont", "VA:Virginia", "WA:Washington",
            "WV:West Virginia", "WI:Wisconsin", "WY:Wyoming");
        add("CA", "AB:Alberta", "BC:British Columbia", "MB:Manitoba", "NB:New Brunswick", "NL:Newfoundland and Labrador", "NS:Nova Scotia",
            "NT:Northwest Territories", "NU:Nunavut", "ON:Ontario", "PE:Prince Edward Island", "QC:Quebec", "SK:Saskatchewan", "YT:Yukon");
        add("AU", "ACT:Australian Capital Territory", "NSW:New South Wales", "NT:Northern Territory", "QLD:Queensland", "SA:South Australia",
            "TAS:Tasmania", "VIC:Victoria", "WA:Western Australia");
    }

    private StateCatalog() {
    }

    private static void add(String country, String... entries) {
        List<State> list = new ArrayList<>();
        Map<String, State> index = new LinkedHashMap<>();
        for (String entry : entries) {
            String[] parts = entry.split(":");
            State state = new State(parts[0], parts[1], parts.length > 2 ? parts[2] : null);
            list.add(state);
            index.put(key(state.code()), state);
            index.put(key(state.name()), state);
            if (state.gstCode() != null) {
                index.put(state.gstCode(), state);
            }
        }
        STATES.put(country, List.copyOf(list));
        INDEX.put(country, index);
    }

    public static List<State> statesOf(String countryCode) {
        return STATES.getOrDefault(countryCode, List.of());
    }

    public static boolean hasCatalog(String countryCode) {
        return STATES.containsKey(countryCode);
    }

    public static Optional<State> find(String countryCode, String text) {
        if (countryCode == null || text == null || text.isBlank()) {
            return Optional.empty();
        }
        Map<String, State> index = INDEX.get(countryCode);
        if (index == null) {
            return Optional.empty();
        }
        String k = key(text);
        State direct = index.get(k);
        if (direct != null) {
            return Optional.of(direct);
        }
        if ("IN".equals(countryCode)) {
            String alias = ALIASES_IN.get(k);
            if (alias != null) {
                return Optional.ofNullable(index.get(key(alias)));
            }
        }
        return Optional.empty();
    }

    /** The comparable key for a state: the catalog code when the country has a catalog and the text is known; the cleaned
     * text for other countries; empty when a catalog country's state is not recognised or the text is blank. */
    public static Optional<String> normalize(String countryCode, String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        if (hasCatalog(countryCode)) {
            return find(countryCode, text).map(State::code);
        }
        String k = key(text);
        return k.isEmpty() ? Optional.empty() : Optional.of(k.length() > 20 ? k.substring(0, 20) : k);
    }

    /** Display name for a normalised key. */
    public static String displayName(String countryCode, String stateKey) {
        if (stateKey == null) {
            return null;
        }
        return find(countryCode, stateKey).map(State::name).orElse(stateKey);
    }

    static String key(String text) {
        String folded = Normalizer.normalize(text.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return folded.toUpperCase(Locale.ROOT).replace("&", "AND").replaceAll("[^A-Z0-9]", "");
    }
}
