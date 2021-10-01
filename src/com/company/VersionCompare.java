package com.company;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionCompare {

	private Integer epoch = 0;

	private List<Object> upStream;

	private List<Object> debian;

	private VersionCompare(String version) {
		Deque<String> temp = new ArrayDeque<>(Arrays.asList(version.split("[\\:]")));
		if (temp.size() > 1) {
			this.epoch = Integer.parseInt(temp.poll());
		}
		temp = new ArrayDeque<>(
			Arrays.asList(
				Objects.requireNonNull(temp.poll())
					.split("[\\-]")
			));
		if (temp.size() > 1) {
			this.debian = getListObject(temp.pollLast());
		} else {
			this.debian = new ArrayList<>();
		}
		this.upStream = getListObject(String.join("-", temp));

	}

	public int compare(VersionCompare target) {
		int result = this.epoch - target.epoch;
		if (result != 0) {
			return result;
		}
		result = compareObject(this.upStream, target.upStream);
		if (result != 0) {
			return result;
		}
		result = compareObject(this.debian, target.debian);

		return result;
	}

	private int compareObject(List<Object> base, List<Object> target) {
		int baseMax = base.size();
		int targetMax = target.size();
		int i = 0;
		for (; i < Math.min(baseMax, targetMax); i++) {
			Object baseObject = base.get(i);
			Object targetObject = target.get(i);
			if (baseObject instanceof String && targetObject instanceof String) {
				int result = compareString((String)baseObject, (String)targetObject);
				if (result != 0) {
					return result;
				}
			} else if (baseObject instanceof Long && targetObject instanceof Long) {
				int result = ((Long)baseObject).compareTo((Long)targetObject);
				if (result != 0) {
					return result;
				}
			} else if (baseObject instanceof Long) {
				return 1;
			} else if (targetObject instanceof Long) {
				return -1;
			}
		}
		if (baseMax < targetMax) {
			return -1;
		} else if (baseMax > targetMax) {
			return 1;
		}
		return 0;
	}

	private int compareString(String base, String target) {
		char[] baseArray = base.toCharArray();
		char[] targetArray = target.toCharArray();
		int baseMax = baseArray.length;
		int targetMax = targetArray.length;
		int i = 0;

		for (; i < Math.min(baseMax, targetMax); i++) {
			int result = baseArray[i] - targetArray[i];
			if (baseArray[i] == '-' || baseArray[i] == '+' || targetArray[i] == '-' || targetArray[i] == '+') {
				result = targetArray[i] - baseArray[i];
			}

			if (result != 0) {
				if (targetArray[i] == '~') {
					return 1;
				}
				if (baseArray[i] == '~') {
					return -1;
				}
				return result > 0 ? 1 : -1;
			}
		}
		if (baseMax < targetMax) {
			return -1;
		} else if (baseMax > targetMax) {
			return 1;
		}
		return 0;
	}

	private List<Object> getListObject(String s) {
		List<Object> objects = new ArrayList<>();
		Pattern p = Pattern.compile("([a-zA-Z])+|([0-9])+|([\\-~+])+");

		Matcher m = p.matcher(s);
		while (m.find()) {
			if (m.group().matches("[0-9]+")) {
				objects.add(Long.parseLong(m.group()));
			} else {
				objects.add(m.group());
			}
		}

		return objects;
	}

	public static boolean isFixedVersion(String base, String target) {
		if (target == null || target.isEmpty() || target.equals("0"))
			return false;
		VersionCompare baseVersion = new VersionCompare(base);
		VersionCompare targetVersion = new VersionCompare(target);
		return baseVersion.compare(targetVersion) >= 0;
	}

	public static int compareTo(String base, String target) {
		VersionCompare baseVersion = new VersionCompare(base);
		VersionCompare targetVersion = new VersionCompare(target);
		return baseVersion.compare(targetVersion);
	}
}