package ru.maklas.jscheme;

import com.google.gson.JsonElement;

import java.util.*;

/** Scheme container for Json objects. Not much use except for printing data to string **/
public class JsonScheme {

	private Map<Path, BaseElementInfo> map;
	private boolean upgraded = false;

	private JsonScheme(Map<Path, BaseElementInfo> map) {
		this.map = map;
	}

	public List<Path> getPaths() {
		return new ArrayList<>(map.keySet());
	}

	public BaseElementInfo getInfo(Path path) {
		return map.get(path);
	}

	public Map<Path, BaseElementInfo> getMap() {
		return map;
	}

	/**
	 * Fills with much more information, such as contents of elements.
	 * @param e must be the same as was used to created JsonScheme
	 */
	public JsonScheme moreInfo(JsonElement e) {
		upgrade();

		for (Map.Entry<Path, BaseElementInfo> entry : map.entrySet()) {
			List<JsonElement> elements = entry.getKey().get(e, false);
			((ExtendedElementInfo) entry.getValue()).process(elements);
		}
		return this;
	}

	/**
	 * Fills with much more information, such as contents of elements.
	 * @param elements must be the same as was used to created JsonScheme
	 */
	public JsonScheme moreInfo(List<JsonElement> elements) {
		upgrade();

		for (Map.Entry<Path, BaseElementInfo> entry : map.entrySet()) {
			ExtendedElementInfo info = (ExtendedElementInfo) entry.getValue();

			for (JsonElement element : elements) {
				List<JsonElement> values = entry.getKey().get(element, false);
				for (JsonElement e : values) {
					info.processElement(e);
				}
			}
			info.postProcess();
		}
		return this;
	}

	private void upgrade(){
		if (!upgraded) {
			for (Map.Entry<Path, BaseElementInfo> entry : map.entrySet()) {
				map.put(entry.getKey(), ExtendedElementInfo.fromBase(entry.getValue()));
			}
			upgraded = true;
		}
	}

	/** Returns list of Paths that are direct children of the specified parent **/
	public List<Path> getDirectChildren(Path parent){
		List<Path> children = new ArrayList<>();
		for (Path path : map.keySet()) {
			if (path.isDirectChildOf(parent)) {
				children.add(path);
			}
		}
		return children;
	}

	/** Returns Parent of this child if it's in the Scheme **/
	public Path getDirectParent(Path child){
		for (Path path : map.keySet()) {
			if (child.isDirectChildOf(path)) {
				return path;
			}
		}
		return null;
	}

	/** Schema for jsons **/
	public static JsonScheme getScheme(List<JsonElement> jsonRoots) {
		Map<Path, BaseElementInfo> map = new LinkedHashMap<>();

		for (JsonElement json : jsonRoots) {
			getScheme(map, new Path(), json);
		}

		for (Map.Entry<Path, BaseElementInfo> entry : map.entrySet()) {
			if (entry.getValue().canBeNull) continue; //This check is only viable if it can't be null

			for (JsonElement json : jsonRoots) {
				List<JsonElement> values = entry.getKey().get(json, false);
				for (JsonElement value : values) {
					if (value == null || value.isJsonNull()) {
						entry.getValue().canBeNull = true;
						break;
					}
				}
				if (entry.getValue().canBeNull) break;
			}
		}

		return new JsonScheme(map);
	}

	/** Schema of the Json tree **/
	public static JsonScheme getScheme(JsonElement e) {
		Map<Path, BaseElementInfo> map = new LinkedHashMap<>();
		getScheme(map, new Path(), e);

		for (Map.Entry<Path, BaseElementInfo> entry : map.entrySet()) {
			List<JsonElement> values = entry.getKey().get(e, false);
			for (JsonElement value : values) {
				if (value == null || value.isJsonNull()){
					entry.getValue().canBeNull = true;
					break;
				}
			}
		}
		return new JsonScheme(map);
	}

	/** Schema of the Json tree **/
	private static void getScheme(Map<Path, BaseElementInfo> map, Path path, JsonElement e) {
		if (e.isJsonObject()){
			Set<String> keys = e.getAsJsonObject().keySet();
			for (String key : keys) {
				final Path elementPath = path.child(key);
				final JsonElement val = e.getAsJsonObject().get(key);
				BaseElementInfo elementInfo = map.get(elementPath);
				if (elementInfo == null){
					elementInfo = new BaseElementInfo(val);
					map.put(elementPath, elementInfo);
				} else {
					elementInfo.update(val);
				}
				if (val == null || val.isJsonNull()){
					elementInfo.update(val);
				} else if (val.isJsonObject()) {
					getScheme(map, elementPath, val);
				} else if (val.isJsonArray()) {
					getScheme(map, elementPath, val);
				} else if (val.isJsonPrimitive()) {
					elementInfo.update(val);
				}
			}
		} else {
			path = path.child("*");
			BaseElementInfo info = map.get(path);
			if (info == null){
				info = new BaseElementInfo(null);
				map.put(path, info);
			}
			for (JsonElement jsonElement : e.getAsJsonArray()) {
				if (jsonElement == null || jsonElement.isJsonNull()){
					info.update(jsonElement);
				} else if (jsonElement.isJsonObject()) {
					info.update(jsonElement);
					getScheme(map, path, jsonElement);
				} else if (jsonElement.isJsonArray()) {
					info.update(jsonElement);
					getScheme(map, path, jsonElement);
				} else if (jsonElement.isJsonPrimitive()) {
					info.update(jsonElement);
				}
			}

		}
	}

	private int maxPathLengthBasic() {
		int length = 0;
		for (Path path : map.keySet()) {
			int l = path.stringLength();
			if (l > length) {
				length = l;
			}
		}
		return length;
	}

	private int maxPathLength() {
		Path longestPath = null;
		int length = 0;
		for (Path path : map.keySet()) {
			int l = getOffset(path) + path.last().length() / 2;
			if (l > length) {
				length = l;
				longestPath = path;
			}
		}
		return (longestPath == null ? 1 : length) + 2;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int padding = maxPathLengthBasic() + 3;

		for (Map.Entry<Path, BaseElementInfo> e : map.entrySet()) {
			sb.append(Utils.rightPad(e.getKey().toString(), padding, ' '))
					.append("-> ")
					.append(e.getValue())
					.append('\n');
		}
		return sb.toString();
	}

	public String toStringTree() {
		StringBuilder sb = new StringBuilder();
		int padding = maxPathLength() + 3;
		Set<Path> paths = map.keySet();
		for (Path path : paths) {
			if (path.depth() == 1) {
				toStringTree(sb, path, padding);
			}
		}
		return sb.toString();
	}

	private void toStringTree(StringBuilder sb, Path root, int padding) {
		sb.append(Utils.rightPad(root.toString(), padding, ' '))
				.append("-> ")
				.append(map.get(root))
				.append('\n');

		List<Path> tracers = new ArrayList<>();

		List<Path> children = getDirectChildren(root);
		if (!children.isEmpty()){
			tracers.add(root);
			for (int i = 0; i < children.size() - 1; i++) {
				append(sb, root, children.get(i), padding, tracers); //With tracing of this parent
			}
			tracers.remove(root);
			append(sb, root, children.get(children.size() - 1), padding, tracers); //Without tracing of this parent for the kids any more
		}
	}

	private void append(StringBuilder sb, Path parent, Path child, int globalPadding, List<Path> tracers){
		int offset = getOffset(parent);
		int padding = globalPadding - offset - 2;
		BaseElementInfo info = map.get(child);

		int start = sb.length();
		sb.append(Utils.repeat(' ', offset));
		markTracers(sb, start, tracers, parent);

		sb.append("-")
				.append(Utils.rightPad(child.last(), padding, ' '))
				.append("-> ")
				.append(info)
				.append('\n');

		List<Path> children = getDirectChildren(child);

		if (!children.isEmpty()){
			tracers.add(child);
			for (int i = 0; i < children.size() - 1; i++) {
				append(sb, child, children.get(i), globalPadding, tracers); //With tracing of this parent
			}
			tracers.remove(child);
			append(sb, child, children.get(children.size() - 1), globalPadding, tracers); //Without tracing of this parent
		}
	}

	private void markTracers(StringBuilder sb, int start, List<Path> tracers, Path parent) {
		for (Path tracer : tracers) {
			markTracer(sb, start, tracer);
		}
		markTracer(sb, start, parent);
	}

	private void markTracer(StringBuilder sb, int start, Path tracer) {
		int pos = start + getOffset(tracer);
		sb.replace(pos, pos + 1, "|");
	}

	private int getOffset(Path parent) {
		int parentLength = 0;
		for (String s : parent.path) {
			parentLength += (Path.isAnySign(s) ? s.length() + 1 : s.length() / 2 + (s.length() % 2) + 1);
		}
		return parentLength;
	}

	private int stringLengthIgnoreAll(Path path){
		return path.stringLength() - 2 - (path.depth() - 1);
	}
}
