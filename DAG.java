import javax.management.relation.RoleUnresolved;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class DAG<T> {

	Node<T> sentinel;
	String filename;
	String oldfilename;
	String xml_name;
	String patch_name;
	String cur_ver;
	int high_branch;
	final String mergefile1 = "merge1.txt";
	final String mergefile2 = "merge2.txt";
	final String key = "#@!%&@%&!$@&!@&%@&16!@^!@*^*!@^*";
	DAG(String filename) {
		sentinel = new Node<T>();
		this.filename = filename;
		this.oldfilename = filename.split("\\.")[0] + "_old.txt";
		this.patch_name = "." + filename;
		this.xml_name = "store.xml";

	}
	public void add(T parent, T child, T child_branch, T comment) {
		if( child== null) {
			return;
		}
		
		if(parent == null) {  //parent is null
			Node<T> child_node = find(child);
			if(child_node == null) { //child does not exist
				this.sentinel.children.add(new Node<>(child, this.sentinel,child_branch,comment));
			}
			else { //if exist, better return....not in hw statement
				return;
			}
		}
		else{ //parent is not null
			Node<T> parent_node = find(parent);
			Node<T> child_node = find(child);
			if(parent_node == null) //this case is not mentioned in the hw statement, but I think it is obvious
				return;
			else if(child_node == null) {
				//make a new node, add it as a child to parent_node, and add parent_node as its parent
				parent_node.children.add(new Node<>(child, parent_node,child_branch,comment)); //check out Node class: Node<>(T c, Node<> parent)
			}
			else {
				//make child_node as a child to parent_node
				parent_node.children.add(child_node);
			}
		}
	}

	protected Node<T> find(T d) {
		if (this.sentinel.children.size() == 0)
			return null;
		ArrayList<Node<T>> graph_nodes = new ArrayList<>();
		graph_nodes.addAll(this.sentinel.getChildren());
		for (int i = 0; i < graph_nodes.size(); i++) {
			if(graph_nodes.get(i).version.equals(d))
				return graph_nodes.get(i);
			if (graph_nodes.get(i).getChildren().size() != 0)
				graph_nodes.addAll(graph_nodes.get(i).getChildren());
		}

		return null;
	}
	
	public boolean search(T data) {
		return find(data) != null;
	}
	
	public void remove(T data) {
		
		Node<T> data_node = find(data);
		if(data_node == null)
			return;
		removeHelper(data_node);
		//We need to nullify the pointer from parent to this node
		data_node.getParent().remove_child(data_node.version);
		data_node.parent = null; //stop pointing to parent....garbage collector will remove this node eventually	
	}
	

	private void removeHelper(Node<T> data_node) {
		if(data_node == null)
			return;
		//remove its children 
		for(Node<T> s: data_node.getChildren()) {
			if(s.parent == data_node) { //we need this check so we do not remove a node that appear in the 
				removeHelper(s);      // children list, but its actual parent is different
				s.parent = null;   
			}
		}
		//clear referneces to its children..this will allow garbage collector to clean 
		// the non-pointed to nodes eventually.
		data_node.getChildren().clear();

	}

	public T lowest_common_ancestor(T data_1, T data_2) {
		if (find(data_1) == null || find(data_2) == null) {
			return null;
		}
		Node<T> dat1 = find(data_1);
		Node<T> dat2 = find(data_2);
		if (dat1 == dat2) {
			return dat1.getElem();
		}
		Stack<Node<T>> stack1 = pathtoX(sentinel,dat1);
		Stack<Node<T>> stack2 = pathtoX(sentinel,dat2);
		T lca = null;
		while(!stack1.isEmpty() && !stack2.isEmpty()){
			Node<T> jpop = stack1.pop();
			Node<T> kpop = stack2.pop();
			if(jpop == kpop) lca = jpop.getElem();
			else{
				break;
			}
		}
		return lca;
	}
	public Stack<Node<T>> pathtoX(Node<T> root, Node<T> data){
		Stack<Node<T>> stack = new Stack<>();
		while(data!=root){
			stack.push(data);
			data = data.getParent();
		}
		return stack;
	}
		
		//return depth of a node
		private int count_depth(Node<T> n) {
			if(n == null) return 0;
			int count =0;
			Node<T> temp = n;
			while(temp !=this.sentinel) {
				temp = temp.parent;
				count++;
			}
			return count;
		}


	///////////////////////////////////// CODE TO PRINT EDGE OF THE GRAPH
	///////////////////////////////////// ///////////////////////////////////

	public void printGraphEdges() {

		if (this.sentinel.getChildren().size() == 0)
			return;

		//// In here I assume that getChildren() will return ArrayList. If you
		//// implementation
		/// returns a linkedList, you need to change ArrayList in the next line to
		//// LinkedList;
		//// and if your getChildren() returns other types of List, just change it to
		//// that
		ArrayList<Node<T>> graph_nodes = new ArrayList<>();
		graph_nodes.addAll(this.sentinel.getChildren());

		for (int i = 0; i < graph_nodes.size(); i++) {
			if (graph_nodes.get(i).getChildren().size() != 0)
				graph_nodes.addAll(graph_nodes.get(i).getChildren());
				//graph_nodes.add(graph_nodes.get(i).children.get(0));
		}

		// Add the nodes to a set to eliminate duplications
		Set<Node<T>> graph_set = new HashSet<Node<T>>();
		for (Node<T> someNode : graph_nodes) {
			graph_set.add(someNode);
		}

		// We have graph nodes stored in a set; iterate over them, and print their
		// edges.
		// Start printing edges: (x , y) means the edge direction is from x --> y.
		for (Node<T> someNode : graph_set) {
			// print edge to parent edge first
			if (someNode.getParent() == this.sentinel) {
				System.out.println("PARENT of " + someNode.getElem().toString() + " is  root/sentinel");
			} else
				System.out.println("PARENT of " + someNode.getElem().toString() + " is "
						+ someNode.getParent().getElem().toString());
			// print children edges
			for (Node<T> c : someNode.getChildren()) {
				System.out.println("(" + someNode.getElem().toString() + ", " + c.getElem().toString() + ")");
			}
		}
	}
	
	

public static void main(String[] args) throws Exception {
	if (args[0].equals("commit")) {
		String filename = args[2];
		String comment = args[4].replaceAll("\"", "");
		DAG comm = new DAG(filename);
		comm = new StaxXMLStreamReader().parseXML(comm.xml_name,comm.filename);
		if(comm.find(comm.cur_ver).children.size() > 0){
			System.out.println("We are doing branch!");
			String newver = comm.updateVer(comm.cur_ver,true);
			comm.commit(newver,true,comment);
		}
		else {
			System.out.println("We are NOT doing branch!");
			String newver = comm.updateVer(comm.cur_ver,false);
			comm.commit(newver,false,comment);
		}

	} else if (args[0].equals("checkout")) {
		String tar = args[2];
		String filename = args[4];
		DAG dag = new DAG(filename);
		dag = (new StaxXMLStreamReader()).parseXML(dag.xml_name,dag.filename);
		dag.checkout(tar,false);

	}
	else if (args[0].equals("merge")){

		String master = args[4];
		String filename = args[2];
		String comment = args[6].replaceAll("\"", "");
		DAG dag = new DAG(filename);
		dag = new StaxXMLStreamReader().parseXML(dag.xml_name,dag.filename);
		dag.merge(master,comment);
	}
	else if (args[0].equals("help")){
		String name = "";
		if(args.length > 1){
			name = args[1];
		}
		help(name);
	}
	else if (args[0].equals("init")){
		DAG dag = new DAG(args[2]);
		String output = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
				"<Data id=\"1.0\">\n" +
				"<High_branch id=\"1\">\n" +
				"<Version id=\"1.0\">\n" +
				"<Comment>You have initialized sfrc.</Comment>\n" +
				"<parent>null</parent>\n" +
				"<branch>1</branch>\n" +
				"</Version>\n" +
				"</High_branch>\n" +
				"</Data>";
		BufferedWriter writer1 = null;
		writer1 = new BufferedWriter(new FileWriter(dag.xml_name));
		writer1.append(output);
		writer1.close();
		BufferedWriter writer2 = null;
		writer2 = new BufferedWriter(new FileWriter(dag.oldfilename));
		writer2.append("");
		writer2.close();
		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter(dag.patch_name));
		writer.append("");
		writer.close();
	}
	else if (args[0].equals("branch")){
		String filename = args[2];
		DAG dag = new DAG(filename);
		dag = new StaxXMLStreamReader().parseXML(dag.xml_name,dag.filename);
		ArrayList<String> branches = dag.branch();
		for( String child: branches){
			System.out.println(child + "\t");
		}
	}
}
private void commit(String newver, Boolean branch, String comment) throws IOException, InterruptedException {
	Runtime r = Runtime.getRuntime();
	//You might need to change -u option for Windows
	String cmd = "diff -u " + this.oldfilename + " " + this.filename;
	String cmd1 = "diff -u " + this.filename + " " + this.oldfilename;
	Process p = r.exec(cmd); // Here we execute the command
	p.waitFor();
	BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
	String line = "";
	String output = "";// Would like to grap all the lines and save them in a single string called
	// output.
	while ((line = b.readLine()) != null) {
		output = output + line + "\n";
	}
	b.close();
	p.destroy();
	p = r.exec(cmd1); // Here we execute the command
	p.waitFor();
	BufferedReader b1 = new BufferedReader(new InputStreamReader(p.getInputStream()));
	String line1 = "";
	String output1 = "";// Would like to grap all the lines and save them in a single string called
	// output.
	while ((line1 = b1.readLine()) != null) {
		output1 = output1 + line1 + "\n";
	}
	b.close();
	p.destroy();
	// Here we write the string containing all the output appeared on the terminal to a file called c.patch
	String curver = this.cur_ver;
	BufferedWriter writer1 = null;
	writer1 = new BufferedWriter(new FileWriter("b.patch"));
	writer1.append(output);
	writer1.close();

	BufferedWriter writer = null;
	writer = new BufferedWriter(new FileWriter(this.patch_name,true));
	String lp = "";
	lp += this.key;
	lp += "diff: " + curver + " -> " + newver;
	lp += "\n";
	String lp1 = "";
	lp1 += this.key;
	lp1 += "diff: " + newver + " -> " + curver;
	lp1 += "\n";
	writer.append(lp);
	writer.append(output);
	writer.append(lp1);
	writer.append(output1);
	writer.close();
	this.updateXML(branch,comment);
	Files.copy(new File(this.filename).toPath(), new File(this.oldfilename).toPath(),StandardCopyOption.REPLACE_EXISTING);
}
public void checkout(String tar_ver, Boolean merge) throws InterruptedException, IOException{
		String cur = this.cur_ver;
		Node curver = this.find((T)cur);
		String cur_branch = (String) curver.branch;
		Node target = this.find((T)tar_ver);
		String tar_branch = (String)target.branch;
		//In the same branch, and the current is later than the checkout target
		if(cur_branch.equals(tar_branch)){
			String[] cur_sp = cur.split("\\.");
			int cur_last = Integer.parseInt(cur_sp[cur_sp.length-1]);
			String[] tar_sp = tar_ver.split("\\.");
			int tar_last = Integer.parseInt(tar_sp[tar_sp.length-1]);
			if(cur_last >= tar_last ) {
				String cur_process = cur;
				while (!cur_process.equals(tar_ver)) {
					cur_process = parsePatch(cur_process,true,"");
				}
			}
			else{
				ArrayList<String> path = record_path(tar_ver,cur_ver);
				String cur_process = cur;
				while (path.size() != 0) {
					String path_child = path.get(0);
					path.remove(0);
					cur_process = parsePatch(cur_process,false,path_child);
				}
			}
		}
		else{
			String lca = (String)lowest_common_ancestor((T)curver.version,(T)tar_ver);
			if(lca.equals(tar_ver)){
				String cur_process = cur;
				while (!cur_process.equals(tar_ver)) {
					cur_process = parsePatch(cur_process,true,"");
				}
			}
			else{
				String cur_process = cur;
				while (!cur_process.equals(lca)) {
					cur_process = parsePatch(cur_process,true,"");
				}
				ArrayList<String> path = record_path(tar_ver,lca);
				while (path.size() != 0) {
					String path_child = path.get(0);
					path.remove(0);
					cur_process = parsePatch(cur_process,false,path_child);
				}
			}

		}
		if(!merge)changecur(tar_ver);
		Files.copy(new File(this.filename).toPath(), new File(this.oldfilename).toPath(),StandardCopyOption.REPLACE_EXISTING);
		//Have not worked for the case that the checkout version is newer than the current one
}
public String parsePatch(String cur_process, Boolean reverse, String path) throws InterruptedException, IOException{
		Node curver = this.find((T)cur_process);
		String message = "";
		String par = "";
		if(reverse) {
			par = (String) curver.parent.version;
			message = this.key + "diff: " + cur_process + " -> " + par;
		}
		else{
			message = path;
			par = path.split(" -> ")[1];
		}
		ArrayList<String> set = new ArrayList<>();
		Scanner sc = new Scanner(new File(this.patch_name));
		while(sc.hasNextLine()){
			set.add(sc.nextLine());
		}
		int begin_line = set.indexOf(message) + 1;
		int end_line = begin_line;
		String end_message = set.get(begin_line);
		while(end_line < set.size() && !(end_message.startsWith(this.key))){
			end_message = set.get(end_line);
			end_line++;
		}
		end_line--;
		String info = "";
		for(int i = begin_line; i <= end_line; i++){
			info += set.get(i) + "\n";
		}
		diffdealer(info,reverse);
		return par;
}
public void diffdealer(String info, Boolean reverse) throws IOException, InterruptedException{
	if (info == "") return;
	BufferedWriter writer = null;
	writer = new BufferedWriter(new FileWriter("b.patch"));
	writer.append(info);
	writer.close();
	if(reverse) {
		Runtime r = Runtime.getRuntime();
		Process p = r.exec("patch -Np1 --ignore-whitespace " + this.filename + " " + "b.patch"); // Here we execute the command
		p.waitFor();
		Files.copy(new File(this.filename).toPath(), new File(this.oldfilename).toPath(),StandardCopyOption.REPLACE_EXISTING);
	}
	else{
		Runtime r = Runtime.getRuntime();
		//You might need to change -u option for Windows
		Process p = r.exec("patch -Np1 --ignore-whitespace " + this.oldfilename + " " + "b.patch"); // Here we execute the command
		p.waitFor();
		Files.copy(new File(this.oldfilename).toPath(), new File(this.filename).toPath(), StandardCopyOption.REPLACE_EXISTING);

	}

}
public ArrayList<String> record_path(String tarver,String curver){
		ArrayList<String> path = new ArrayList<>();
		while(!tarver.equals(curver)){
			String info = this.key + "diff: ";
			String cur_par = (String)this.find((T)tarver).parent.version;
			info += cur_par + " -> " + tarver;
			path.add(info);
			tarver = cur_par;
		}
		Collections.reverse(path);
		return path;

}

	//It is used to write XML. It has three main purpose: add a child to the previous version. Add a new version. Update the current version to the committed version.
	//However, I have not done it for update branch and patch file, but I don't think it will be a heavy job.
	//And there are some bugs for adding quote marks, I will solve them tomorrow.
	public String updateVer(String cur_ver, Boolean branch){
		String newver = "";
		if(branch){
			newver = cur_ver + "." + "1";
		}
		else {
			String[] sp = cur_ver.split("\\.");
			int newver_2 = Integer.parseInt(sp[sp.length-1]) + 1;
			for(int i = 0;i < sp.length-1; i++){
				newver += sp[i] + ".";
			}
			newver += Integer.toString(newver_2);

		}
		return newver;
	}
public void updateXML(Boolean branch, String comment) throws FileNotFoundException, IOException{
		Scanner sc = new Scanner(new File(this.xml_name));
		ArrayList<String> set = new ArrayList<>();
		while(sc.hasNextLine()){
			set.add(sc.nextLine().strip());
		}
		String output = set.get(0) + "\n";
		String textToEdit1 = "<Version id=" + "\"" + this.cur_ver + "\"" +">";
		int count = set.indexOf(textToEdit1);
		String new_ver = updateVer(this.cur_ver,branch);
		String new_cur = "<Data id=" +"\""+new_ver+"\""+">" ;
		output += new_cur + "\n";
		int new_high = this.high_branch + 1;
		if(!branch) {
			output += set.get(2) + "\n";
		}
		else{
			output += "<High_branch id=" + "\"" + new_high + "\"" + ">" + "\n";
		}
		for(int i =3;i<count;i++){
			output += set.get(i) + "\n";
		}
		String edit = textToEdit1 + "\n"+ "<child>" + new_ver + "</child>" + "\n";
		output += edit;
		for(int i = count+1; i< set.size()-2;i++){
			output += set.get(i) + "\n";
		}
		output += "<Version id=" + "\"" + new_ver + "\"" +">" + "\n";
		output += "<Comment>" + comment + "</Comment>" + "\n";
		output += "<parent>" + this.cur_ver + "</parent>" + "\n";
		if(!branch) {
			output += "<branch>" + this.find((T)this.cur_ver).branch + "</branch>" + "\n";
		}
		else{
			output += "<branch>" + new_high + "</branch>" + "\n";
		}
		output += "</Version>" + "\n";
		output += "</High_branch>" + "\n";
		output += "</Data>" + "\n";
		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter(xml_name,false));
		writer.write(output);
		writer.close();

	}
	public void changecur(String new_ver) throws FileNotFoundException, IOException{
		String curver = "";
		Scanner sc = new Scanner(new File(this.xml_name));
		ArrayList<String> set = new ArrayList<>();
		while(sc.hasNextLine()){
			set.add(sc.nextLine().strip());
		}

		String new_cur = "<Data id=" +"\""+new_ver+"\""+">" ;
		set.set(1,new_cur);
		String output = "";
		for(String child: set){
			output += child + "\n";
		}
		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter(this.xml_name));
		writer.write(output);
		writer.close();
	}
	private static void help(String name) throws IOException, InterruptedException{
		switch(name){
			case "":
				System.out.println("sfrc.jar help 'command'\n" +
						"    Displays help for commands.\n" +
						"\n" +
						"sfrc.jar commit -f file -m 'string'\n" +
						"\n" +
						"sfrc.jar checkout -rev revision -f file \n" +
						"    Refresh the working copy of the file from its history file. The revision number is the version to be retrieved. User could replace the revision number by a branch name.\n" +
						"\n" +
						"sfrc.jar merge -f file -rev revision -m comment\n" +
						"    You can only merge a not master branch to the current master branch, and you can only do it when you are now in your latest version in Main Branch \n" +
						"sfrc.jar branch -list A.txt \n" +
						"		List all branches we have and the first branch is the current used one." +
								"sfrc.jar init -f \n" +
						"			Initialize srfc and create store.xml, .A.txt and A_old.txt");
				break;
			case "commit":
				System.out.println("sfrc.jar commit -f file -m string"+"\n"
						+"Example: sfrc.jar commit -f A.txt -m change to the file");
				break;
			case "checkout":
				System.out.println("sfrc.jar checkout -rev revision -f file\n" +
						"    Refresh the working copy of the file from its history file. The revision number is the version to be retrieved. User could replace the revision number by a branch name."+"\n"
						+"Example: sfrc.jar checkout -rev 1.5 -f A.txt ");
				break;
			case "merge":
				System.out.println("sfrc.jar merge -f file -rev revision \n" +
						"  You can only merge a not master branch to the current master branch, and you can only do it when you are now in your latest version in Main Branch"+"\n"
						+"Example: sfrc.jar merge -f A.txt -rev 1.2.2 -m 'I have done a merge'");
				break;
			case "branch":
				System.out.println("sfrc.jar branch -list A.txt \n" +
						"		List all branches we have and the first branch is the current used one.");
				break;
			case "init":
				System.out.println("sfrc.jar init -f \n" +
						"			Initialize srfc and create store.xml, .A.txt and A_old.txt");
				break;
		}
	}


	private void sdiff(String branch) throws IOException, InterruptedException {
		Runtime r = Runtime.getRuntime();
		Files.copy(new File(this.filename).toPath(), new File(this.mergefile1).toPath(),StandardCopyOption.REPLACE_EXISTING);
		this.checkout(branch,true);
		Files.copy(new File(this.filename).toPath(), new File(this.mergefile2).toPath(),StandardCopyOption.REPLACE_EXISTING);
		//You might need to change -u option for Window
		Process p = r.exec(("sdiff -l ") + this.mergefile1 + " " + this.mergefile2); // Here we execute the command
		p.waitFor();
		BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line="";
		String output="";
		while ((line = b.readLine()) != null) {
			output = output + line + "\n";
		}
		b.close();

		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter("sdiff.txt"));
		writer.write(output);
		writer.close();
	}

	private void merge(String branch, String comment) throws IOException, InterruptedException {
		String curver = this.cur_ver;
		if(Integer.parseInt((String)this.find((T)this.cur_ver).branch) != 1){
			System.out.println("You need to first get your current version to the latest version in Main Branch.");
			return;
		}
		if(Integer.parseInt((String)this.find((T)branch).branch) == 1){
			System.out.println("You need to merge a revision from another branch");
			return;
		}
		sdiff(branch);
		File file = new File("sdiff.txt");
		if (file.length()==0) {
			System.out.println("Successfully committed");

		}else{
			BufferedReader br = new BufferedReader (new FileReader(file));
			String message = "There is a merge conflict" + "\n"
					+ "Give one of the following choice to resolve conflict" + "\n"
					+ "1 : merge only the left line" + "\n"
					+ "2 : merge only the right line" + "\n"
					+ "3 : discard both lines" + "\n"
					+ "4 : abort operation";
			String st;
			String pattern = "      [\\(|<|>|\\)|\\||\\\\|\\/][\\t|\\n]";
			String output = "";
			boolean abort = true;
			while ((st = br.readLine()) != null) {
				st = st + '\n'; //This is needed because readLine() strip the '\n' at the end of line
				String[] tokensVal = st.split(pattern);
				if (tokensVal.length == 1) {
					output += tokensVal[0] + "\n";
				}
				if (tokensVal.length == 2) {
					System.out.println(message);
					System.out.println("\n" + "The lines are:" + "\n"
							+ "left line: " + tokensVal[0] + "\n"
							+ "right line: " + tokensVal[1]);
					Scanner sc = new Scanner(System.in);
					int i = sc.nextInt();
					switch (i) {
						case 1:
							output = output + tokensVal[0] + "\n";
							break;
						case 2:
							output = output + tokensVal[1] + "\n";
							break;
						case 3:
							break;
						case 4:
							abort = false;
							break;
					}
				}
				if (!abort) break;
			}
			if (!abort){
				System.out.println("Merge aborted");
				Files.copy(new File(this.mergefile1).toPath(), new File(this.filename).toPath(),StandardCopyOption.REPLACE_EXISTING);
				Files.copy(new File(this.mergefile1).toPath(), new File(this.oldfilename).toPath(),StandardCopyOption.REPLACE_EXISTING);
				return;
			}
			else {
				System.out.println("Successfully committed");
				BufferedWriter writer = null;
				writer = new BufferedWriter(new FileWriter(this.filename));
				System.out.println(output);
				writer.write(output);
				writer.close();
			}
			String newver = updateVer(this.cur_ver,false);
			this.cur_ver = curver;
			Files.copy(new File(this.mergefile1).toPath(), new File(this.oldfilename).toPath(),StandardCopyOption.REPLACE_EXISTING);
			commit(newver, false,comment);
		}
	}

	public ArrayList<String> branch() throws FileNotFoundException{
		ArrayList<String> branches = new ArrayList<>();
		branches.add((String)this.find((T)cur_ver).branch);
		Scanner sc = new Scanner(new File(this.xml_name));
		ArrayList<String> set = new ArrayList<>();
		while(sc.hasNextLine()){
			set.add(sc.nextLine().strip());
		}
		for (String child: set){
			if(child.startsWith("<branch>")){
				if(!branches.contains(child.substring(8,9)))
				branches.add(child.substring(8,9));
			}
		}
		return branches;
	}
}

