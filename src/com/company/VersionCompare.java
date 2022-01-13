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

	private final List<Object> upStream;

	private final List<Object> debian;

	private VersionCompare(String version) {
		Deque<String> deque = new ArrayDeque<>(Arrays.asList(version.split("[\\:]")));
		if (deque.size() > 1) {
			this.epoch = Integer.parseInt(deque.poll());
		}
		deque = new ArrayDeque<>(
			Arrays.asList(
				Objects.requireNonNull(deque.poll())
					.split("[\\-]")
			));
		if (deque.size() > 1) {
			this.debian = getListObject(deque.pollLast());
		} else {
			this.debian = new ArrayList<>();
		}
		this.upStream = getListObject(String.join("-", deque));

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
			if (isString(baseObject) && isString(targetObject)) {
				int result = compareString((String)baseObject, (String)targetObject);
				if (result != 0) {
					return result;
				}
			} else if (isLong(baseObject) && isLong(targetObject)) {
				int result = ((Long)baseObject).compareTo((Long)targetObject);
				if (result != 0) {
					return result;
				}
			} else if (isLong(baseObject)) {
				return 1;
			} else if (isLong(targetObject)) {
				return -1;
			}
		}
		if (baseMax < targetMax) {
			if (isString(target.get(i)) && isTilde((String)target.get(i))) {
				return 1;
			}
			return -1;
		} else if (baseMax > targetMax) {
			if (isString(base.get(i)) && isTilde((String)base.get(i))) {
				return -1;
			}
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
		int result = 0;

		for (; i < Math.min(baseMax, targetMax); i++) {
			result = baseArray[i] - targetArray[i];
			if (isPlusAndMinus(baseArray[i]) && isPlusAndMinus(targetArray[i])) {
				result = targetArray[i] - baseArray[i];
			}
			if (result != 0) {
				if (isTilde(targetArray[i])) {
					result = 1;
					break;
				} else if (isTilde(baseArray[i])) {
					result = -1;
					break;
				}
				return result > 0 ? 1 : -1;
			}
		}
		if (baseMax < targetMax && isTilde(targetArray[i])) {
			result = 1;
		} else if (baseMax > targetMax && isTilde(baseArray[i])) {
			result = -1;
		}
		return result;
	}

	private List<Object> getListObject(String s) {
		List<Object> objects = new ArrayList<>();
		Pattern p = Pattern.compile("[a-zA-Z]+|[0-9]+|[\\-~+]+");

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

	public static int compareTo(String base, String target) {
		if (base == null && target == null) {
			return 0;
		} else if (base == null) {
			return -1;
		} else if (target == null) {
			return 1;
		}
		VersionCompare baseVersion = new VersionCompare(base);
		VersionCompare targetVersion = new VersionCompare(target);
		return baseVersion.compare(targetVersion);
	}

	public static boolean isFixedVersion(String base, String target) {
		if (target != null && !target.isEmpty() && !target.equals("0")) {
			VersionCompare baseVersion = new VersionCompare(base);
			VersionCompare targetVersion = new VersionCompare(target);
			return baseVersion.compare(targetVersion) >= 0;
		}
		return false;
	}

	public static boolean isVulnerableVersion(String base, String target) {
		if (target != null && !target.isEmpty() && !target.equals("0")) {
			VersionCompare baseVersion = new VersionCompare(base);
			VersionCompare targetVersion = new VersionCompare(target);
			return baseVersion.compare(targetVersion) < 0;
		}
		return false;
	}

	public static boolean isFixedVersion(String status, String base, String target) {
		if (target != null && !target.isEmpty() && !target.equals("0") && status.equals("resolved")) {
			VersionCompare baseVersion = new VersionCompare(base);
			VersionCompare targetVersion = new VersionCompare(target);
			return baseVersion.compare(targetVersion) >= 0;
		}
		return false;
	}

	public static boolean isVulnerableVersion(String status, String base, String target) {
		if (target != null && !target.isEmpty() && !target.equals("0") && status.equals("resolved")) {
			VersionCompare baseVersion = new VersionCompare(base);
			VersionCompare targetVersion = new VersionCompare(target);
			return baseVersion.compare(targetVersion) < 0;
		}
		return false;
	}

	private boolean isPlusAndMinus(char text) {
		return text == '-' || text == '+';
	}

	private boolean isTilde(char text) {
		return text == '~';
	}

	private boolean isTilde(String text) {
		return text.startsWith("~");
	}

	private boolean isString(Object object) {
		return object instanceof String;
	}

	private boolean isLong(Object object) {
		return object instanceof Long;
	}

}