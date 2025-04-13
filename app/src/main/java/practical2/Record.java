package practical2;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Record {

    @JacksonXmlProperty(localName = "name")
    public String name;

    @JacksonXmlProperty(localName = "postalZip")
    public String postalZip;

    @JacksonXmlProperty(localName = "region")
    public String region;

    @JacksonXmlProperty(localName = "country")
    public String country;

    @JacksonXmlProperty(localName = "address")
    public String address;

    @JacksonXmlProperty(localName = "list")
    private String listRaw;

    public List<Integer> getList() {
        if (listRaw == null || listRaw.trim().isEmpty()) {
            return List.of();  // return empty list
        }
    
        return Arrays.stream(listRaw.split(","))
                     .map(String::trim)
                     .filter(s -> !s.isEmpty()) // avoid parsing empty strings
                     .map(Integer::parseInt)
                     .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("Name: %s\nPostalZip: %s\nRegion: %s\nCountry: %s\nAddress: %s\nList: %s\n",
                name, postalZip, region, country, address, getList());
    }
}

