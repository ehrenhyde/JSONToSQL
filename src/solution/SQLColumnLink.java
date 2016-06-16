package solution;

public class SQLColumnLink implements Comparable<SQLColumnLink>{
	private SQLColumnReference parentColRef;
	private String parentAssociatedProp;
	
	public SQLColumnLink(SQLColumnReference parentColRef,String parentAssociatedProp){
		this.parentColRef = parentColRef;
		this.parentAssociatedProp = parentAssociatedProp;
	}
	
	public SQLColumnReference getParentColRef(){
		return parentColRef;
	}
	
	public String getParentAssociatedProp(){
		return parentAssociatedProp;
	}
	
	@Override
	public String toString(){
		return "parentColRef: " + parentColRef.toString() + " parentAssociatedProp: " + parentAssociatedProp;
	}
	
	@Override
	public int compareTo(SQLColumnLink otherColLink) {
		String otherColLinkString =otherColLink.toString();
		String thisColLinkString = this.toString();
		if (otherColLinkString.equals(thisColLinkString)){
			return 0;
		}else{
			return -1;
		}
	}

	public boolean equalsColLink(SQLColumnLink otherColLink) {
		String otherColLinkString =otherColLink.toString();
		String thisColLinkString = this.toString();
		return (otherColLinkString.equals(thisColLinkString));
	}
}
