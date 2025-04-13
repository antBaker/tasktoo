/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package practical2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;


import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {

    // Class representing the fields we want to extract from each record.
    static class Record {
        String name;
        String postalZip;
        String region;
        String country;
        String address;
        List<Integer> list;

        public Record() {
            list = new ArrayList<>();
        }

        @Override
        public String toString() {
            return "Record{" +
                    "name='" + name + '\'' +
                    ", postalZip='" + postalZip + '\'' +
                    ", region='" + region + '\'' +
                    ", country='" + country + '\'' +
                    ", address='" + address + '\'' +
                    ", list=" + list +
                    '}';
        }
    }

    // Custom SAX handler to process XML events.
    static class RecordHandler extends DefaultHandler {
        private List<Record> records = new ArrayList<>();
        private Record currentRecord;
        private StringBuilder currentValue = new StringBuilder();
        private String currentElement; // track current element

        public List<Record> getRecords() {
            return records;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            currentValue.setLength(0); // clear the characters buffer
            if ("record".equalsIgnoreCase(qName)) {
                currentRecord = new Record();
            }
            currentElement = qName;
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            currentValue.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            String content = currentValue.toString().trim();
            if (currentRecord != null) {
                switch (qName.toLowerCase()) {
                    case "name":
                        currentRecord.name = content;
                        break;
                    case "postalzip":
                        currentRecord.postalZip = content;
                        break;
                    case "region":
                        currentRecord.region = content;
                        break;
                    case "country":
                        currentRecord.country = content;
                        break;
                    case "address":
                        currentRecord.address = content;
                        break;
                    case "list":
                        if (!content.isEmpty()) {
                            String[] items = content.split(",");
                            for (String item : items) {
                                try {
                                    if (!item.trim().isEmpty()) {
                                        currentRecord.list.add(Integer.parseInt(item.trim()));
                                    }
                                } catch (NumberFormatException nfe) {
                                    System.err.println("Warning: Unable to parse number from '" + item.trim() + "'. Skipping.");
                                }
                            }
                        }
                        break;
                    case "record":
                        // Finished processing a record element; add to list.
                        records.add(currentRecord);
                        currentRecord = null;
                        break;
                    default:
                        // Ignore other elements.
                }
            }
            currentElement = null;
        }
    }

    public static void main(String[] args) {
        try {
            // Parse the XML file using SAX
            List<Record> records = parseXMLWithSAX("data.xml");
            if (records.isEmpty()) {
                System.err.println("No records found in the XML file.");
                return;
            }

            // Process command-line arguments: split by commas and trim them.
            Set<String> selectedFields = new HashSet<>();
            if (args.length == 0) {
                System.out.println("Please provide fields to display (e.g., name,postalZip,region,list)");
                return;
            } else {
                for (String arg : args) {
                    String[] tokens = arg.split(",");
                    for (String token : tokens) {
                        if (!token.trim().isEmpty()) {
                            selectedFields.add(token.trim().toLowerCase());
                        }
                    }
                }
            }
            if (selectedFields.isEmpty()) {
                System.err.println("No valid fields were provided. Please try again.");
                return;
            }

            // Prepare a list of maps containing only selected fields for each record.
            List<Map<String, Object>> jsonRecords = new ArrayList<>();
            for (Record record : records) {
                Map<String, Object> selectedValues = new LinkedHashMap<>();

                if (selectedFields.contains("name")) {
                    selectedValues.put("name", safeValue(record.name));
                }
                if (selectedFields.contains("postalzip")) {
                    selectedValues.put("postalZip", safeValue(record.postalZip));
                }
                if (selectedFields.contains("region")) {
                    selectedValues.put("region", safeValue(record.region));
                }
                if (selectedFields.contains("country")) {
                    selectedValues.put("country", safeValue(record.country));
                }
                if (selectedFields.contains("address")) {
                    selectedValues.put("address", safeValue(record.address));
                }
                if (selectedFields.contains("list")) {
                    selectedValues.put("list", record.list != null ? record.list : Collections.emptyList());
                }

                if (!selectedValues.isEmpty()) {
                    jsonRecords.add(selectedValues);
                }
            }

            // Convert the list of maps to JSON using Jackson.
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonRecords);
            System.out.println(jsonResult);
        } catch (Exception e) {
            System.err.println("An error occurred:");
            e.printStackTrace();
        }
    }

    /**
     * Parses the XML file located in resources using the SAX parser.
     * @param resourceName The name of the resource (e.g., "data.xml").
     * @return A List of Record objects parsed from the XML.
     * @throws Exception if there are issues loading or parsing the resource.
     */
    private static List<Record> parseXMLWithSAX(String resourceName) throws Exception {
        InputStream input = Main.class.getClassLoader().getResourceAsStream(resourceName);
        if (input == null) {
            throw new Exception("Resource not found: " + resourceName);
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        RecordHandler handler = new RecordHandler();

        // Wrap the InputStream in an InputSource with UTF-8 encoding.
        InputSource is = new InputSource(new InputStreamReader(input, StandardCharsets.UTF_8));
        is.setEncoding("UTF-8");

        saxParser.parse(is, handler);
        return handler.getRecords();
    }

    /**
     * Returns a safe string value or "N/A" if the value is null or empty.
     */
    private static String safeValue(String value) {
        return (value != null && !value.isEmpty()) ? value : "N/A";
    }
}
