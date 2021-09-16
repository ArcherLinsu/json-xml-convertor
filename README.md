# json-xml-convertor

因为个人需要，写的json和xml的互转工具，主要是为了解决使用jackson转换时发现的如下问题：

1. 特殊情况下，单个xml节点需要转换为json数组类型；通过xml节点属性值来判断是否转换，业务相关，可自行修改。
2. xml转换为json时，是否忽略节点属性；
3. json转换为xml的时候，为数组类型节点添加属性；

使用示例，具体查看`test`目录下的测试代码：

```java
//xml转换为json，默认忽略xml节点属性
String json1 = Converter.xml2json(xml);
//xml转换为json，不忽略节点属性
String json2 = Converter.xml2json(xml,true);
```

```java
//json转换为xml，使用默认xml根节点名称
String xml1 = Converter.json2xml(json);
//json转换为xml，自定义xml根节点名称
String xml2 = Converter.json2xml(json,"XML");
```

