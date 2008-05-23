public class B{

    public int i=1;

    /*@ public normal_behavior
      @  ensures \result==true;
      @*/
    public boolean test(){
	A a = new A();
	A.InnerA ia = a.new A.InnerA();
	InnerB ib = new InnerB(a);
	return ia.m()==ib.m() && ia.m()==a.i && ib.n()==i;
    }

    class InnerB extends A.InnerA{
	
	public InnerB(A a){
	    a.super();
	}

	/*@ public normal_behavior
	  @  ensures \result==B.this.i;
	  @*/
	public int n(){
	    return i;
	}

    }

}
