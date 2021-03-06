package com.tourismmer.app.json;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.tourismmer.app.model.User;
import com.tourismmer.app.util.Util;

public class UserSerializer extends JsonSerializer<User> {

	@Override
	public void serialize(User o, JsonGenerator jgen, SerializerProvider sp)
			throws IOException, JsonProcessingException {
		
		jgen.writeStartObject();
		
		if(Util.isNotEmptyOrNullOrZero(o.getStatusCode())) jgen.writeObjectField("statusCode", o.getStatusCode());
		if(Util.isNotEmptyOrNullOrZero(o.getStatusText())) jgen.writeObjectField("statusText", o.getStatusText());
		
		jgen.writeObjectField("id", o.getId());
		if(Util.isNotEmptyOrNullOrZero(o.getName())) jgen.writeObjectField("name", o.getName());
		if(Util.isNotEmptyOrNullOrZero(o.getBirthday())) jgen.writeObjectField("birthday", o.getBirthday());
		if(Util.isNotEmptyOrNullOrZero(o.getEmail())) jgen.writeObjectField("email", o.getEmail());
		if(Util.isNotEmptyOrNullOrZero(o.getGender())) jgen.writeObjectField("gender", o.getGender());
		if(Util.isNotEmptyOrNullOrZero(o.getFacebookId())) jgen.writeObjectField("facebookId", o.getFacebookId());
		
		jgen.writeEndObject();
		
		
	}

}
