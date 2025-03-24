package me.ram.bedwarsitemshop.config;

import lombok.Getter;

@Getter
public enum EnumLocale {
	EN_US("en_US"),
	ZH_CN("zh_CN"),
	ZH_TW("zh_TW");

	private final String name;

	EnumLocale(String name) {
		this.name = name;
	}

    public static EnumLocale getByName(String n) {
		for (EnumLocale type : values()) {
			if (type.getName().equals(n)) {
				return type;
			}
		}
		return null;
	}
}
