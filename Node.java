
import java.util.*;
// We will still keep the parent and children be their original type. Actually we don't need to worry about
// add a parent to the current version but without other attributes like parent and children to this parent node.
// It does not matter because the only thing we care about is the version of current' parent node but not any thing
// else.
public class Node<T> {
	public  T version;
	public T branch;
	public T parent_ver;
	public T comment;
    public  Node<T> parent;
    public ArrayList<Node<T>> children ;

    public  Node(T data, Node<T> parent){
        this.version = data;
        this.parent = parent;
        children = new ArrayList<Node<T>>();
  }
    public  Node(T data, Node<T> parent, T branch, T comment){
        this.version = data;
        this.branch = branch;
        this.parent = parent;
        this.comment = comment;
        children = new ArrayList<Node<T>>();
    }
  public  Node(T data){
        this.version = data;
        this.parent = null;
        this.branch = null;
        children = new ArrayList<Node<T>>();

  }
  
  Node(){
	  this.version = null;
	  this.parent = null;
	  this.branch = null;
      children = new ArrayList<Node<T>>();
  }
  
  public void remove_child(T c) {
	  
	  int index_of_data_node = -1;
	  for(Node<T> q: getChildren()) {
			index_of_data_node ++;
			if(q.version.equals(c))
				break;
		}
		if(index_of_data_node >= 0)
			this.getChildren().remove(index_of_data_node);

  }
  
  public ArrayList<Node<T>> getChildren() {
      return this.children;
  }
  public void add_children(Node<T> child){
        this.children.add(child);
  }

  public Node<T> getParent(){
      return this.parent;
  }
  public void setParent(Node<T> parent){
      this.parent=parent;
  }

  public T getElem(){
      return this.version;
  }
  
  @Override
  public String toString() { 
      return version.toString();
  } 

  
  @Override
  public boolean equals(Object obj) {
	  if(obj == null)
		  return false;
	  if(obj.getClass() != this.getClass())
		  return false;
	  T temp = (T) obj;
	  
	  if(this.version.equals(temp))
		  return true;
	  return false;
  }
    
}