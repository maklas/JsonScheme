package ru.maklas.jscheme;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/** Represents information about JsonElement. What types this element can be. **/
public class BaseElementInfo {

	/** Types that this Element can have. Usually one, but it's not like it's forbidden to use multiple **/
	Set<Class> types = new HashSet<>();
	/** Whether or not this Element was found to be null or didn't exist **/
	boolean canBeNull = false;
	int minArrLength = Integer.MAX_VALUE;
	int maxArrLength = 0;

	public BaseElementInfo(@Nullable JsonElement e) {
		if (e != null) {
			update(e);
		} else {

		}
	}

	BaseElementInfo() {

	}

	public void update(JsonElement element){
		Class type = getType(element);
		if (type != null){
			types.add(type);
		}
		if (type == JsonArray.class){
			int size = element.getAsJsonArray().size();
			if (size > maxArrLength){
				maxArrLength = size;
			}
			if (size < minArrLength){
				minArrLength = size;
			}
		}
		canBeNull = canBeNull || element == null;
	}

	public boolean hasType(Class type){
		return types.contains(type);
	}

	public boolean isNullable(){
		return canBeNull;
	}

	private static Class getType(JsonElement e){
		if (e == null || e.isJsonNull()) return null;
		if (e.isJsonObject()) return JsonObject.class;
		if (e.isJsonArray()) return JsonArray.class;
		if (e.isJsonPrimitive()) {
			if (e.getAsJsonPrimitive().isBoolean()) return Boolean.class;
			if (e.getAsJsonPrimitive().isNumber()) return Number.class;
			if (e.getAsJsonPrimitive().isString()) return String.class;
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		if (types.size() == 0) {
			sb.append("null");
		} else {
			for (Class type : types) {
				sb.append(type.getSimpleName());
				if (type == JsonArray.class && minArrLength != Integer.MAX_VALUE) {
					if (minArrLength == maxArrLength) {
						sb.append(" ").append(minArrLength);
					} else {
						sb.append(" ").append(minArrLength).append("..").append(maxArrLength);
					}
				}
				sb.append(", ");
			}
			sb.setLength(sb.length() - 2);
		}

		sb.append("]");

		if (canBeNull){
			sb.append(" nullable");
		}
		return sb.toString();
	}
}