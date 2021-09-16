package priv.linsu.tool.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class ConverterTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final SAXReader saxReader = new SAXReader();

    @Test
    void xml2json() throws IOException, DocumentException {
        String path = "src/test/resources/message_xml.xml";
        String xml = saxReader.read(Files.newInputStream(Paths.get(path))).asXML();
        //默认忽略节点属性
        String json = Converter.xml2json(xml);
        System.out.println(objectMapper.readTree(json).toPrettyString());
    }

    @Test
    void testXml2json() throws IOException, DocumentException {
        String path = "src/test/resources/message_xml.xml";
        String xml = saxReader.read(Files.newInputStream(Paths.get(path))).asXML();
        //转换时是否忽略节点属性
        String json = Converter.xml2json(xml, true);
        System.out.println(objectMapper.readTree(json).toPrettyString());
    }

    @Test
    void json2xml() throws IOException {
        String path = "src/test/resources/message_json.json";
        String json = objectMapper.readTree(Files.newInputStream(Paths.get(path))).toString();
        String xml = Converter.json2xml(json);
        System.out.println(xml);
    }

    @Test
    void testJson2xml() throws IOException {
        String path = "src/test/resources/message_json.json";
        String json = objectMapper.readTree(Files.newInputStream(Paths.get(path))).toString();
        String xml = Converter.json2xml(json,"XML");
        System.out.println(xml);
    }
}