package io.github.lucasssoh.boussole.format.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * The field of knowledge a track belongs to, from the detailed fields of the UNESCO
 * International Standard Classification of Education (ISCED-F 2013), whose code each keeps;
 * information technology stays one field, as in its narrow level; and one field of Boussole's own,
 * {@link #CRITICAL_THINKING}. Written in {@code subject.yaml} as {@code domain}.
 */
public enum Domain {
    PERSONAL_DEVELOPMENT("personal-development", "0031"),
    EDUCATION("education", "011"),
    AUDIOVISUAL_MEDIA("audiovisual-media", "0211"),
    DESIGN("design", "0212"),
    FINE_ARTS("fine-arts", "0213"),
    CRAFTS("crafts", "0214"),
    MUSIC_AND_PERFORMING_ARTS("music-and-performing-arts", "0215"),
    RELIGION_AND_THEOLOGY("religion-and-theology", "0221"),
    HISTORY_AND_ARCHAEOLOGY("history-and-archaeology", "0222"),
    PHILOSOPHY_AND_ETHICS("philosophy-and-ethics", "0223"),
    LANGUAGES("languages", "0231"),
    LITERATURE_AND_LINGUISTICS("literature-and-linguistics", "0232"),
    ECONOMICS("economics", "0311"),
    POLITICAL_SCIENCE("political-science", "0312"),
    PSYCHOLOGY("psychology", "0313"),
    SOCIOLOGY_AND_CULTURAL_STUDIES("sociology-and-cultural-studies", "0314"),
    JOURNALISM("journalism", "0321"),
    LIBRARY_AND_ARCHIVES("library-and-archives", "0322"),
    BUSINESS_AND_MANAGEMENT("business-and-management", "041"),
    LAW("law", "0421"),
    BIOLOGY("biology", "0511"),
    BIOCHEMISTRY("biochemistry", "0512"),
    ENVIRONMENTAL_SCIENCES("environmental-sciences", "052"),
    CHEMISTRY("chemistry", "0531"),
    EARTH_SCIENCES("earth-sciences", "0532"),
    PHYSICS("physics", "0533"),
    MATHEMATICS("mathematics", "0541"),
    STATISTICS("statistics", "0542"),
    INFORMATION_TECHNOLOGY("information-technology", "061"),
    ENGINEERING("engineering", "071"),
    MANUFACTURING("manufacturing", "072"),
    ARCHITECTURE_AND_CONSTRUCTION("architecture-and-construction", "073"),
    AGRICULTURE("agriculture", "081"),
    FORESTRY_AND_FISHERIES("forestry-and-fisheries", "082"),
    VETERINARY("veterinary", "0841"),
    MEDICINE("medicine", "0912"),
    DENTISTRY("dentistry", "0911"),
    NURSING_AND_CARE("nursing-and-care", "0913"),
    PHARMACY("pharmacy", "0916"),
    HEALTH_TECHNOLOGY_AND_THERAPY("health-technology-and-therapy", "0914"),
    SOCIAL_WORK("social-work", "092"),
    HOSPITALITY_AND_TOURISM("hospitality-and-tourism", "1013"),
    SPORTS("sports", "1014"),
    PERSONAL_SERVICES("personal-services", "101"),
    SECURITY_AND_DEFENCE("security-and-defence", "103"),
    TRANSPORT("transport", "104"),
    /**
     * Boussole's own field, outside ISCED: epistemology, logic, zetetics, the scientific method,
     * cognitive biases; thinking well about what one knows. Grand Sage asks for it.
     */
    CRITICAL_THINKING("critical-thinking", "");

    private final String id;
    private final String isced;

    Domain(String id, String isced) {
        this.id = id;
        this.isced = isced;
    }

    /** As written in {@code subject.yaml}: {@code information-technology}, {@code philosophy-and-ethics}… */
    public String id() {
        return id;
    }

    /** Its ISCED-F 2013 code; empty for a field of Boussole's own. */
    public String isced() {
        return isced;
    }

    public static Optional<Domain> of(String id) {
        return Arrays.stream(values()).filter(domain -> domain.id.equals(id)).findFirst();
    }
}
