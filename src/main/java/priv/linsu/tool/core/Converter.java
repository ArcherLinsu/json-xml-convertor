package priv.linsu.tool.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dom4j.*;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Converter {
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * xml -> json
     *
     * @param xml
     * @return
     * @throws DocumentException
     */
    public static String xml2json(String xml) throws DocumentException {
        Document doc = DocumentHelper.parseText(xml);
        Element xmlRoot = doc.getRootElement();
        ObjectNode jsonRoot = objectMapper.createObjectNode();
        xml2json(xmlRoot.elements(), jsonRoot, false);
        return jsonRoot.toString();
    }

    /**
     * xml -> json
     *
     * @param xml
     * @param hasAttr 是否将xml节点的属性一块转换
     * @return
     * @throws DocumentException
     */
    public static String xml2json(String xml, boolean hasAttr) throws DocumentException {
        Document doc = DocumentHelper.parseText(xml);
        Element xmlRoot = doc.getRootElement();
        ObjectNode jsonRoot = objectMapper.createObjectNode();
        xml2json(xmlRoot.elements(), jsonRoot, hasAttr);
        return jsonRoot.toString();
    }

    /**
     * json -> xml
     *
     * @param json
     * @return
     * @throws JsonProcessingException
     */
    public static String json2xml(String json) throws JsonProcessingException {
        Element xmlRoot = DocumentHelper.createElement("JSON");
        JsonNode jsonRoot = objectMapper.readTree(json);
        json2xml(jsonRoot,xmlRoot);
        return xmlRoot.asXML();
    }

    /**
     * json -> xml
     *
     * @param json
     * @param rootName 设置转换后xml的根节点名称
     * @return
     * @throws JsonProcessingException
     */
    public static String json2xml(String json, String rootName) throws JsonProcessingException {
        Element xmlRoot = DocumentHelper.createElement(rootName);
        JsonNode jsonRoot = objectMapper.readTree(json);
        json2xml(jsonRoot,xmlRoot);
        return xmlRoot.asXML();
    }

    /**
     * xml -> json
     *
     * @param xmlNodes
     * @param jsonNode
     * @param hasAttr
     */
    private static void xml2json(List<Element> xmlNodes, ObjectNode jsonNode, boolean hasAttr) {
        //记录同名节点，避免重复处理
        Set processed = new HashSet();
        for (Element xmlNode : xmlNodes) {
            //如果该节点已处理过，不再处理
            if (processed.contains(xmlNode.getName())) {
                continue;
            }
            processed.add(xmlNode.getName());
            //获取同级同名节点
            List<Element> sameNodes = xmlNode.getParent().selectNodes(xmlNode.getName());
            //非数组类型处理
            if (sameNodes.size() == 1 && !isArray(xmlNode)) {
                //文本节点处理
                if (xmlNode.isTextOnly()) {
                    List<Attribute> attributes = xmlNode.attributes();
                    //节点属性处理
                    if (hasAttr && attributes.size() > 0) {
                        jsonNode.set(xmlNode.getName(), buildAttr(xmlNode, attributes));
                        continue;
                    }
                    jsonNode.put(xmlNode.getName(), xmlNode.getText());
                    continue;
                }
                ObjectNode newJsonNode = objectMapper.createObjectNode();
                jsonNode.set(xmlNode.getName(), newJsonNode);
                xml2json(xmlNode.elements(), newJsonNode, hasAttr);
                continue;
            }
            //构建Json数组对象
            ArrayNode newArrayNode = objectMapper.createArrayNode();
            jsonNode.set(xmlNode.getName(), newArrayNode);
            //遍历同名节点处理
            for (Element sameNode : sameNodes) {
                List<Attribute> attributes = sameNode.attributes();
                //文本节点处理
                if (sameNode.isTextOnly()) {
                    //节点属性处理
                    if (hasAttr && attributes.size() > 0) {
                        jsonNode.set(xmlNode.getName(), buildAttr(sameNode, attributes));
                        continue;
                    }
                    newArrayNode.add(sameNode.getText());
                    continue;
                }
                ObjectNode newJsonNode = objectMapper.createObjectNode();
                newArrayNode.add(newJsonNode);
                if (hasAttr && attributes.size() > 0) {
                    attributes.forEach(attribute -> {
                        newJsonNode.put(attribute.getName(), String.valueOf(attribute.getData()));
                    });
                }
                xml2json(sameNode.elements(), newJsonNode, hasAttr);
            }
        }
    }

    private static void json2xml(JsonNode jsonNode, Element xmlNode) {
        jsonNode.fields().forEachRemaining(jsonNodeEntry -> {
            //普通节点
            if (jsonNodeEntry.getValue().isValueNode()) {
                xmlNode.addElement(jsonNodeEntry.getKey()).setText(jsonNodeEntry.getValue().asText());
            }
            //有子节点
            if (jsonNodeEntry.getValue().isContainerNode()) {
                //数组节点
                if (jsonNodeEntry.getValue().isArray()) {
                    for (int i = 0; i < jsonNodeEntry.getValue().size(); i++) {
                        Element lineNode = xmlNode.addElement(jsonNodeEntry.getKey());
                        lineNode.addAttribute("id", String.valueOf(i + 1));
                        lineNode.addAttribute("type", "array");
                        json2xml(jsonNodeEntry.getValue().get(i), lineNode);
                    }
                } else {
                    Element newXmlNode = xmlNode.addElement(jsonNodeEntry.getKey());
                    json2xml(jsonNodeEntry.getValue(), newXmlNode);
                }
            }
        });
    }

    /**
     * 判断一个单xml节点是否应该转为json数组
     *
     * @param element
     * @return
     */
    private static boolean isArray(Element element) {
        Attribute attribute = element.attribute("type");
        return Objects.nonNull(attribute) && "array".equals(attribute.getData());
    }

    /**
     * 将xml节点和该节点属性一起转为json对象
     *
     * @param element
     * @param attributes
     * @return
     */
    private static ObjectNode buildAttr(Element element, List<Attribute> attributes) {
        ObjectNode newJsonNode = objectMapper.createObjectNode();
        newJsonNode.put("", element.getText());
        attributes.forEach(attribute -> {
            newJsonNode.put(attribute.getName(), String.valueOf(attribute.getData()));
        });
        return newJsonNode;
    }
}
