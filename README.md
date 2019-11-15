Feed your Json to the `JsonScheme.class` and get scheme for your Json with some additional info, if required.
Such as average values, null percentage, most frequent string values, avg array length etc.

[Get it on Jitpack!](https://jitpack.io/#maklas/JsonScheme)

Usage:
```java
public static void main(String[] args) {
	String jsonString = getJsonString();
	JsonElement json = new JsonParser().parse(jsonString);
	JsonScheme scheme = JsonScheme.getScheme(json).moreInfo(json);
	System.out.println(scheme.toStringTree());
}

private static String getJsonString() {
	return "{\"widget\":{\"debug\":\"on\",\"window\":{\"title\":\"Sample Konfabulator Widget\",\"name\":\"main_window\",\"width\":500,\"height\":500},\"image\":{\"src\":\"Images/Sun.png\",\"name\":\"sun1\",\"size\":250},\"text\":{\"name\":\"text1\",\"bold\":false,\"size\":36,\"hOffset\":250,\"alignment\":\"center\",\"array\":[{\"StringKey\":\"StringValue\",\"IntegerKey\":123456.9,\"BooleanKey\":false},{\"StringKey\":\"StringValue\",\"IntegerKey\":300,\"BooleanKey\":true},{\"StringKey\":\"OtherStringValue\",\"IntegerKey\":250,\"BooleanKey\":true}]}}}";
}
```

Result:
```
[widget]                 -> [JsonObject] 
   |-debug               -> [String always 'on'] 
   |-window              -> [JsonObject] 
   |  |-title            -> [String always 'Sample Konfabulator ...'] 
   |  |-name             -> [String always 'main_window'] 
   |  |-width            -> [Number Avg: 500.0] 
   |  |-height           -> [Number Avg: 500.0] 
   |-image               -> [JsonObject] 
   |  |-src              -> [String always 'Images/Sun.png'] 
   |  |-name             -> [String always 'sun1'] 
   |  |-size             -> [Number Avg: 250.0] 
   |-text                -> [JsonObject] 
     |-name              -> [String always 'text1'] 
     |-bold              -> [Boolean 0/1] 
     |-size              -> [Number Avg: 36.0] 
     |-hOffset           -> [Number Avg: 250.0] 
     |-alignment         -> [String always 'center'] 
     |-array             -> [JsonArray 3] 
        |-*              -> [JsonObject] 
          |-StringKey    -> [String Most used: 'StringValue'(2) Repeatable: 1 unique: 1] 
          |-IntegerKey   -> [Number Avg: 41335.6] 
          |-BooleanKey   -> [Boolean 2/3] 
```

Output format:

`[<Type> <Field information>?] <Percentage of null values>?`