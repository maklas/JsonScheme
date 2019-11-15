Feed your Json to the `JsonScheme.class` and get scheme for your Json with some additional info, if required.
Such as average values, null percentage, most frequent string values, avg array length etc.

[Get it on Jitpack!](https://jitpack.io/#maklas/JsonScheme)

Works with Gson.

Usage:
```java
public static void main(String[] args) {
	String jsonString = getJsonString();
	JsonElement json = new JsonParser().parse(jsonString);
	JsonScheme scheme = JsonScheme.getScheme(json).moreInfo(json);
	System.out.println(scheme.toStringTree());
}

private static String getJsonString() {
	return "{\"widget\":{\"debug\":\"on\",\"window\":{\"name\":\"main_window\",\"width\":500,\"height\":500},\"image\":{\"name\":\"sun\",\"size\":250},\"text\":{\"name\":\"text\",\"bold\":false,\"size\":36,\"offset\":250,\"alignment\":\"center\",\"array\":[{\"StringKey\":\"StringValue\",\"IntegerKey\":5,\"BooleanKey\":false,\"NullableKey\":\"Value\"},{\"StringKey\":\"StringValue\",\"IntegerKey\":123456.9,\"BooleanKey\":false,\"NullableKey\":\"Value2\"},{\"StringKey\":\"OtherStringValue\",\"IntegerKey\":300,\"BooleanKey\":true,\"NullableKey\":null},{\"StringKey\":\"AnotherStringValue\",\"IntegerKey\":250,\"BooleanKey\":true}]},\"primitiveArray\":[\"A\",\"B\",\"C\",\"D\",\"B\",\"C\",\"B\",\"E\",5,true,false,10]}}";
}
```

Result:
```
[widget]                      -> [JsonObject] 
    |-debug                   -> [String always 'on'] 
    |-window                  -> [JsonObject] 
    |   |-name                -> [String always 'main_window'] 
    |   |-width               -> [Number Avg: 500.0] 
    |   |-height              -> [Number Avg: 500.0] 
    |-image                   -> [JsonObject] 
    |   |-name                -> [String always 'sun'] 
    |   |-size                -> [Number Avg: 250.0] 
    |-text                    -> [JsonObject] 
    |  |-name                 -> [String always 'text'] 
    |  |-bold                 -> [Boolean 0/1] 
    |  |-size                 -> [Number Avg: 36.0] 
    |  |-offset               -> [Number Avg: 250.0] 
    |  |-alignment            -> [String always 'center'] 
    |  |-array                -> [JsonArray 4] 
    |      |-*                -> [JsonObject] 
    |        |-StringKey      -> [String Most used: 'StringValue'(2) Repeatable: 1 unique: 2] 
    |        |-IntegerKey     -> [Number Avg: 31003.0] 
    |        |-BooleanKey     -> [Boolean 2/4] 
    |        |-NullableKey    -> [String unique: 2] 50.0% NON NULL
    |-primitiveArray          -> [JsonArray 12] 
            |-*               -> [Boolean 1/2] [Number Avg: 7.5] [String Most used: 'B'(3) Repeatable: 2 unique: 3] 
```

Output format:

`[<Type> <Field information>?] <Percentage of null values>?`