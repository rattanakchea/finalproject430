
public class TestSimple {

	public static void main(String[] args) {
		char[] string = new char[10];
		for (int i = 0; i < 5; i++)
			string[i] = 'a';
		
		String com = "aaaaa";
		String str = String.valueOf(string, 0, 5);
		
		System.out.println(str.length());
		System.out.println(com);
		System.out.println(str);
		if (com.equals(str)){
			System.out.println("equal");
		}else{
			System.out.println("not equal");
		}
	}

}
