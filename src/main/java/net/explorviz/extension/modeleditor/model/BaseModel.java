package net.explorviz.extension.modeleditor.model;

import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.github.jasminb.jsonapi.LongIdHandler;
import com.github.jasminb.jsonapi.annotations.Id;

//Needed for cyclical serialization
@JsonIdentityInfo(generator = ObjectIdGenerators.StringIdGenerator.class, property = "id")
public class BaseModel {

	private static final AtomicLong ID_GENERATOR = new AtomicLong();

	@Id(LongIdHandler.class)
	private Long id;

	public BaseModel() {
		id = ID_GENERATOR.incrementAndGet();
	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

}
