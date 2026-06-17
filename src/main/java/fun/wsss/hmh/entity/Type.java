package fun.wsss.hmh.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Type {

	private Integer id;
	private String type;
	
	public Type() {
		super();
	}
	public Type(Integer id, String type) {
		super();
		this.id = id;
		this.type = type;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
}
