package ru.maklas.jscheme;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents path for Json elements.
 * Examples:
 * <li><b>key</b></li>
 * <li><b>parentKey/childKey</b></li>
 * <li><b>[parentKey/childKey]</b></li>
 * <li><b>/parentKey/childKey</b></li>
 * <li><b>anArray/0</b> - first element of the array</li>
 * <li><b>anArray/*</b> - All elements of the array</li>
 * <li><b>rootElement/childArray/*<b>/</b>subElement/5/stringKey</b> - For all <b>childArray</b>s, get <b>subElement</b>'s fifth object and get value of <b>stringKey</b></li>
 */
public class Path {

	final String[] path;

	public Path(String... path) {
		this.path = Objects.requireNonNull(path);
	}

	/**
	 * Default way to create instance of a Path
	 * For semantics, see {@link Path}
	 */
	public static Path parse(String s) {
		s = Objects.requireNonNull(s).trim();
		if (s.startsWith("[") && s.endsWith("]")){
			s = s.substring(1, s.length() - 1);
		}
		while (s.startsWith("/")) {
			s = s.substring(1);
		}
		String[] paths = s.split("/");
		for (int i = 0; i < paths.length; i++) {
			paths[i] = paths[i].trim();
		}
		return new Path(paths);
	}

	@Override
	public int hashCode() {
		int hash = 0;
		for (int i = 0; i < path.length; i++) {
			hash = 31 * hash + path[i].hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Path && Arrays.equals(path, ((Path) obj).path);
	}

	/** @see #get(JsonElement, boolean) **/
	public List<JsonElement> get(JsonElement e){
		return get(e, true);
	}

	/**
	 * Returns List of JsonElements that reside by this path inside this element
	 * @param e where to apply path
	 * @param removeNulls removes null values from output
	 */
	public List<JsonElement> get(JsonElement e, boolean removeNulls){
		List<JsonElement> parents = new ArrayList<>();
		List<JsonElement> children = new ArrayList<>();
		parents.add(e);

		for (int i = 0; i < path.length - 1; i++) {
			String s = path[i].trim();
			for (JsonElement parent : parents) {
				if (parent == null || parent.isJsonNull()) {
					continue;
				}

				if (!parent.isJsonObject() && !parent.isJsonArray()){
					throw new RuntimeException("Dead end " + parent.getClass().getSimpleName());
				}

				if (parent.isJsonObject()){
					if (isAnySign(s)){
						for (String key : parent.getAsJsonObject().keySet()) {
							children.add(parent.getAsJsonObject().get(key));
						}
					} else {
						children.add(parent.getAsJsonObject().get(s));
					}
				} else {
					JsonArray arr = parent.getAsJsonArray();
					if (isAnySign(s)){
						for (JsonElement jsonElement : arr) {
							children.add(jsonElement);
						}
					} else {
						int index = Integer.parseInt(s);
						if (index < arr.size()) {
							children.add(arr.get(index));
						}
					}
				}
			}
			if (children.size() == 0) return children;
			parents.clear();
			parents.addAll(children);
			children.clear();
		}

		String lastPath = path[path.length - 1].trim();
		if (removeNulls){
			for (JsonElement parent : parents) {
				if (parent == null || parent.isJsonNull()) continue;
				if (parent.isJsonArray()) {
					JsonArray arr = parent.getAsJsonArray();
					if (isAnySign(lastPath)) {
						for (JsonElement jsonElement : arr) {
							if (jsonElement != null && !jsonElement.isJsonNull()) {
								children.add(jsonElement);
							}
						}
					} else {
						int index = Integer.parseInt(lastPath);
						if (index < arr.size()) {
							JsonElement e1 = arr.get(index);
							if (e1 != null && !e1.isJsonNull()) {
								children.add(e1);
							}
						}
					}
				} else if (parent.isJsonObject()) {
					JsonElement e1 = parent.getAsJsonObject().get(lastPath);
					if (e1 != null && !e1.isJsonNull()) {
						children.add(e1);
					}
				}
			}
		} else {
			for (JsonElement parent : parents) {
				if (parent == null || parent.isJsonNull()) continue;
				if (parent.isJsonArray()) {
					JsonArray arr = parent.getAsJsonArray();
					if (isAnySign(lastPath)) {
						for (JsonElement jsonElement : arr) {
							children.add(jsonElement);
						}
					} else {
						int index = Integer.parseInt(lastPath);
						if (index < arr.size()) {
							children.add(arr.get(index));
						}
					}
				} else if (parent.isJsonObject()) {
					children.add(parent.getAsJsonObject().get(lastPath));
				}
			}
		}

		return children;
	}

	/** Same as {@link #get(JsonElement, boolean)}, but makes it easier to analyze lists of Json files **/
	public List<JsonElement> get(List<JsonElement> elements, boolean removeNulls){
		List<JsonElement> array = new ArrayList<>();
		for (JsonElement element : elements) {
			array.addAll(get(element, removeNulls));
		}
		return array;
	}

	public boolean isArrayElement(){
		return path.length != 0 && isAnySign(last());
	}

	static boolean isAnySign(String s){
		return "*".equals(s);
	}

	public Integer getInt(JsonElement e) {
		JsonElement val = getSingle(e);
		return val == null ? null : val.getAsInt();
	}

	public Long getLong(JsonElement e) {
		JsonElement val = getSingle(e);
		return val == null ? null : val.getAsLong();
	}

	public Float getFloat(JsonElement e) {
		JsonElement val = getSingle(e);
		return val == null ? null : val.getAsFloat();
	}

	public Double getDouble(JsonElement e) {
		JsonElement val = getSingle(e);
		return val == null ? null : val.getAsDouble();
	}

	public Boolean getBool(JsonElement e) {
		JsonElement val = getSingle(e);
		return val == null ? null : val.getAsBoolean();
	}

	public String getString(JsonElement e) {
		JsonElement val = getSingle(e);
		return val == null ? null : val.getAsString();
	}

	public JsonObject getObject(JsonElement e) {
		JsonElement val = getSingle(e);
		return val == null ? null : val.getAsJsonObject();
	}

	public JsonArray getArray(JsonElement e) {
		JsonElement val = getSingle(e);
		return val == null ? null : val.getAsJsonArray();
	}

	/** Returns single JsonElement, implying that there are no arrays in the path. Otherwise might throw exception **/
	public JsonElement getSingle(JsonElement e) {
		for (String s : path) {
			if (e == null || e.isJsonNull()) {
				return null;
			}
			if (!e.isJsonObject() && !e.isJsonArray()){
				throw new RuntimeException("Can't access field '" + s + "' inside primitive of type " + e.getClass().getSimpleName());
			}
			if (e.isJsonObject()){
				e = e.getAsJsonObject().get(s);
			} else {
				JsonArray arr = e.getAsJsonArray();
				int index = Integer.parseInt(s.trim());
				e = arr.get(index);
			}
		}
		return e;
	}

	/** Returns first JsonElement found by this path **/
	public JsonElement getFirst(JsonElement e) {
		List<JsonElement> elements = get(e);
		return elements.isEmpty() ? null : elements.get(0);
	}

	/** Appends at the end of current path **/
	public Path child(String... path){
		String[] p = new String[this.path.length + path.length];
		System.arraycopy(this.path, 0, p, 0, this.path.length);
		System.arraycopy(path, 0, p, this.path.length, path.length);
		return new Path(p);
	}

	/**
	 * Whether or not this path, passed as a parameter is a child of this path.
	 * <li>Path.parse("root/one/two").isChildOf(Path.parse("root/one")) will return <b>true</b></li>
	 * <li>Path.parse("root/one/two").isChildOf(Path.parse("root")) will return <b>false</b></li>
	 */
	public boolean isDirectChildOf(Path p) {
		return p.path.length == path.length - 1 && isChildOf(p);
	}

	/**
	 * Whether or not this path, passed as a parameter is a child of this path.
	 * <li>Path.parse("root/one/two").isChildOf(Path.parse("root/one")) will return <b>true</b></li>
	 * <li>Path.parse("root/one/two").isChildOf(Path.parse("root")) will return <b>true</b></li>
	 */
	public boolean isChildOf(Path p) {
		if (p.path.length >= path.length) return false;

		for (int i = 0; i < p.path.length; i++) {
			if (!path[i].equals(p.path[i])){
				return false;
			}
		}
		return true;
	}

	/** The last element in the path. Empty string if it's null**/
	public String last(){
		return path.length == 0 ? "" : path[path.length - 1];
	}

	/** Returns parent's path. Null if there is no parent **/
	public Path parent() {
		if (path.length == 0) return null;
		String[] parent = new String[this.path.length - 1];
		System.arraycopy(this.path, 0, parent, 0, parent.length);
		return new Path(parent);
	}

	@Override
	public String toString() {
		if (path.length == 0) return "[]";
		StringBuilder sb = new StringBuilder("[").append(path[0]);
		for (int i = 1; i < path.length; i++) {
			sb.append("/").append(path[i]);
		}
		sb.append("]");
		return sb.toString();
	}

	/** Length of this path's declaration. Equals to #toString().length() **/
	int stringLength() {
		if (path.length == 0) return 2;
		int l = 1 + path[0].length();
		for (int i = 1; i < path.length; i++) {
			l += 1 + path[i].length();
		}
		return l + 1;
	}

	public int depth() {
		return path.length;
	}
}