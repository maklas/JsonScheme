package ru.maklas.jscheme;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

/** More information about Elements **/
public class ExtendedElementInfo extends BaseElementInfo {

	private Map<String, Integer> values; //Считаем множественные стринги
	private int uniqueStringValues; //Количество уникальных стрингов. Не будут содержатся в values
	//Для определения ср. знач.
	private int numberCount; //Количество цифр
	private double numberSum; //Сумма цифр
	//Подсчёт среднего значения boolean.
	private int booleanCount;
	private int booleanTrue;
	//Считаем количество не null объектов.
	private int totalCount;
	private int nonNullObjectCount;
	//Тоже самое, но для длинны массивов.
	private int arrayCount;
	private int arraySizeSum;

	public ExtendedElementInfo() {
		values = new HashMap<>();
	}

	static ExtendedElementInfo fromBase(BaseElementInfo e) {
		ExtendedElementInfo extInfo = new ExtendedElementInfo();
		extInfo.canBeNull = e.canBeNull;
		extInfo.minArrLength = e.minArrLength;
		extInfo.maxArrLength = e.maxArrLength;
		extInfo.types = new HashSet<>(e.types);
		return extInfo;
	}

	void process(List<JsonElement> elements) {
		for (JsonElement element : elements) {
			processElement(element);
		}
		postProcess();
	}

	void processElement(JsonElement e){
		totalCount++;
		if (e == null || e.isJsonNull()) return;
		nonNullObjectCount++;

		if (e.isJsonPrimitive()) {
			if (e.getAsJsonPrimitive().isString()) {
				String string = e.getAsString();
				Integer count = values.get(string);
				count = count == null ? 1 : count + 1;
				values.put(string, count);
			} else if (e.getAsJsonPrimitive().isNumber()) {
				numberCount++;
				numberSum += e.getAsJsonPrimitive().getAsDouble();
			} else if (e.getAsJsonPrimitive().isBoolean()) {
				booleanCount++;
				if (e.getAsBoolean()) {
					booleanTrue++;
				}
			}
		} else if (e.isJsonArray()) {
			arrayCount++;
			arraySizeSum += e.getAsJsonArray().size();
		}
	}

	void postProcess(){
		if (values.size() == 1) return;
		Iterator<Map.Entry<String, Integer>> iterator = values.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Integer> next = iterator.next();
			if (next.getValue() != null && next.getValue() == 1){
				iterator.remove();
				uniqueStringValues++;
			}
		}
	}

	@Override
	public String toString() {
		if (types.size() == 0) return "[null]";
		StringBuilder sb = new StringBuilder();

		for (Class type : types) {
			if (type.equals(JsonObject.class)) {
				sb.append("[JsonObject] ");
			} else if (type.equals(JsonArray.class)) {
				sb.append("[");
				sb.append(type.getSimpleName()).append(" ");
				if (minArrLength == maxArrLength) {
					sb.append(minArrLength);
				} else if (minArrLength != Integer.MAX_VALUE){
					sb.append(minArrLength).append("..").append(maxArrLength);
					if (arrayCount > 0) {
						sb.append(" (Avg: ").append(Utils.df(((double) arraySizeSum) / arrayCount)).append(")");
					}
				}
				sb.append("] ");
			} else if (type.equals(String.class)) {
				sb.append("[").append(type.getSimpleName());
				String stringStats = getStringStats();
				if (stringStats.length() > 0) {
					sb.append(" ").append(stringStats);
				}
				sb.append("] ");
			} else if (type.equals(Number.class)) {
				sb.append("[");
				sb.append(type.getSimpleName());
				if (numberCount > 0) {
					sb.append(" Avg: ").append(Utils.df(numberSum / numberCount));
				}
				sb.append("] ");
			} else if (type.equals(Boolean.class)) {
				sb.append("[");
				sb.append(type.getSimpleName());
				if (booleanCount > 0) {
					sb.append(" ").append(booleanTrue).append("/").append(booleanCount);
				}
				sb.append("] ");
			}
		}

		if (canBeNull){
			double percentage = ((nonNullObjectCount * 1.0) / totalCount) * 100;
			sb.append(Utils.df(percentage)).append("% NON NULL");
		}

		return sb.toString();
	}

	private String getStringStats() {
		if (values.size() == 0 && uniqueStringValues == 0) return "";
		if (values.size() == 1 && uniqueStringValues == 0) {
			String[] val = values.keySet().toArray(new String[0]);
			return "always '" + Utils.limit(val[0], 20, "...") + "'";
		}
		StringBuilder sb = new StringBuilder();
		int maxCount = 0;
		String maxVal = null;
		for (Map.Entry<String, Integer> e : values.entrySet()) {
			if (e.getValue() != null && e.getValue() > maxCount) {
				maxCount = e.getValue();
				maxVal = e.getKey();
			}
		}
		if (maxVal != null) {
			sb.append("Most used: '")
					.append(Utils.limit(maxVal, 20, "..."))
					.append("'")
					.append("(").append(maxCount).append(") ");
		}
		if (values.size() > 0) {
			sb.append("Repeatable: ").append(values.size()).append(" ");
		}
		sb.append("unique: ").append(uniqueStringValues);
		return sb.toString();
	}
}
