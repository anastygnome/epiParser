package com.example.parsertest.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.example.parsertest.configuration.ConfigurationLoader;
import com.example.parsertest.entities.Epigraphe;

import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to handle XML parsing for Epigraphe objects using SAX parser.
 * This class provides methods to extract an Epigraphe from an XML file
 */
@Slf4j
public final class EpicherchelParser {
    private static final SAXParserFactory FACTORY = SAXParserFactory.newInstance();

    private EpicherchelParser() {
        // Private constructor to prevent instantiation
        throw new IllegalStateException("Utility class, cannot instantiate");
    }

    /**
     * 
     * Opens a stream from a given URL to fetch XML data.
     *
     * @param xmlURI The URL from which to fetch the XML.
     * @return An InputStream to read the XML data.
     * @throws IOException        If an I/O error occurs.
     * @throws URISyntaxException If the URL syntax is incorrect.
     */
    private static InputStream getXMLStreamFromURI(URI xmlURI) throws IOException {
        return xmlURI.toURL().openStream();
    }

    /**
     * Gets an Epigraph from Epicherchell with a given ID.
     *
     * @param id The ID of the epigraphe to be fetched.
     * @return An Epigraphe object populated from the Epicherchell data.
     */
    public static Epigraphe getEpigraphe(int id) {

        URI epURI = UriBuilder.fromUri(ConfigurationLoader.EPICHERCHELL_URI).queryParam("id", id).build();
        return extractContentFromXML(id, epURI);
    }

    /**
     * Extracts an Epigraphe object from XML data from a given ressource.
     *
     * @param id     The ID of the epigraphe to be fetched.
     * @param xmlURI URI of the XML document.
     * @return An Epigraphe object populated from the XML data.
     */
    private static Epigraphe extractContentFromXML(int id, URI xmlURI) {
        Epigraphe epigraphe = Epigraphe.newInstance(id);
        try (InputStream inputStream = getXMLStreamFromURI(xmlURI)) {
            SAXParser saxParser = FACTORY.newSAXParser();
            XMLHandler handler = new XMLHandler(epigraphe);
            InputSource source = new InputSource(inputStream);
            saxParser.parse(source, handler);
            if (epigraphe.getImgUrl().isEmpty()) {
                epigraphe.setImgUrl(null);
            }
            return epigraphe;
        } catch (Exception e) {
            log.error("Error parsing XML for Epigraphe", e);
            return null;
        }
    }

    /**
     * SAX handler to process XML elements and populate an Epigraphe object.
     */
    static class XMLHandler extends DefaultHandler {
        private final Epigraphe epigraphe;
        private final StringBuilder currentValue = new StringBuilder();
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        private boolean inFacSimile = false;
        private boolean inTranslation = false;
        private boolean inEdition = false;
        private boolean inName = false;

        /**
         * Constructs an XMLHandler.
         *
         * @param epigraphe The Epigraphe object to populate.
         */
        public XMLHandler(Epigraphe epigraphe) {
            this.epigraphe = epigraphe;
        }

        public static DateTimeFormatter getDateFormatter() {
            return DATE_FORMATTER;
        }

        /**
         * Handles date elements by parsing and setting the date in the Epigraphe.
         *
         * @param text The date string to parse.
         */
        private void handleDate(String text) {
            try {
                if (!text.isEmpty()) {
                    epigraphe.setDate(LocalDate.parse(text, DATE_FORMATTER));
                }
            } catch (DateTimeParseException e) {
                log.error(e.toString());
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            // Called at the start of an element. We use this method to handle elements like
            // <date>, <persName>, <facsimile>, <graphic>, and <div> by setting flags or
            // processing attributes.

            switch (qName) {
                case "date" -> handleDate(attributes.getValue("when"));
                case "persName" -> inName = true; // Set flag to indicate we are inside a &<persName> element
                case "facsimile" -> inFacSimile = true; // Set flag to indicate we are inside a <facsimile> element
                case "graphic" -> {
                    // If inside a <facsimile> element, add the image URL to the Epigraphe's image
                    // list
                    if (inFacSimile) {
                        epigraphe.getImgUrl().add(attributes.getValue("url"));
                    }
                }
                case "div" -> {
                    // Handle different types of <div> elements based on their "type" attribute
                    String type = attributes.getValue("type");
                    if ("translation".equals(type)) {
                        inTranslation = true; // Set flag to indicate we are inside a translation <div>
                    } else if ("edition".equals(type)) {
                        inEdition = true; // Set flag to indicate we are inside an edition <div>
                    }
                }
                case "lb" -> currentValue.append("\n"); // Append a newline for <lb>, corresponding to a line break
                default -> currentValue.setLength(0); // Clear the current value buffer
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            // Called with the text contents inside an element. We accumulate this text
            // in currentValue if we are inside elements we're interested in.

            if (inEdition || inTranslation || inName) {
                currentValue.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            // Called at the end of an element. We use this method to process the
            // accumulated text
            // in currentValue and reset flags as needed.

            switch (qName) {
                case "facsimile" -> inFacSimile = false; // Reset flag at the end of <facsimile>
                case "ab" -> {
                    // Process the accumulated text when ending an <ab> element
                    String finalString = currentValue.toString().strip();
                    if (inTranslation) {
                        epigraphe.setTranslation(finalString);
                        inTranslation = false;
                    } else if (inEdition) {
                        epigraphe.setOriginal(finalString);
                        inEdition = false;
                    }
                    currentValue.setLength(0);
                }
                case "div" -> {
                    // Reset flags at the end of a <div> element
                    inTranslation = false;
                    inEdition = false;
                }
                case "persName" -> {
                    // Set the name in the Epigraphe object, clean up and reset the flag
                    epigraphe.setName(currentValue.toString().strip());
                    currentValue.setLength(0);
                    inName = false;
                }
                default -> {
                    // No operation for other end elements
                }
            }
        }
    }
}
