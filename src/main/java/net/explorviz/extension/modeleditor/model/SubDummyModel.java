package net.explorviz.extension.modeleditor.model;

import com.github.jasminb.jsonapi.annotations.Type;

@Type("sub-dummy")
public class SubDummyModel extends BaseModel {

	private long value;

	public SubDummyModel() {
		// default constructor for JSON API parsing
	}

	public SubDummyModel(final long value) {
		this.value = value;
	}

	public void setSubDummyValue(final long value) {
		this.value = value;
	}

	public long getSubDummyValue() {
		return value;
	}

}
