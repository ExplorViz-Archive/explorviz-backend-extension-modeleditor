package net.explorviz.extension.modeleditor.model;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("dummy")
public class DummyModel extends BaseModel {

	private String dummyName;

	@Relationship("sub-dummy")
	private SubDummyModel subDummy;

	public DummyModel() {
		// default constructor for JSON API parsing
	}

	public DummyModel(final String dummyName, final SubDummyModel subDummy) {
		this.dummyName = dummyName;
		this.subDummy = subDummy;
	}

	public String getDummyName() {
		return dummyName;
	}

	public void setDummyName(final String dummyName) {
		this.dummyName = dummyName;
	}

	public SubDummyModel getSubDummy() {
		return subDummy;
	}

	public void setSubDummy(final SubDummyModel subDummy) {
		this.subDummy = subDummy;
	}

}
