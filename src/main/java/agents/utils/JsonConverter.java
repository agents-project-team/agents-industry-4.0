package agents.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConverter {

	public static String toJsonString(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	public static <T> T fromJsonString(String content, Class<T> valueType) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(content, valueType);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

}
