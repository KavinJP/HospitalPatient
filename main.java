import java.util.*;

class Patient implements Comparable<Patient>
{
	String pid,na,dis;
	int sesc;
	double bilam;
	Patient(String id,String name,String disease,int score,double bamount)
	{
		pid=id;
		na=name;
		dis=disease;
		sesc=score;
		bilam=bamount;
	}
	public String getPatientID()
	{
		return pid;
	}
	public String getName()
	{
		return na;
	}
	public String getDisease()
	{
		return dis;
	}
	public int getSeverityScore()
	{
		return sesc;
	}
	public double getBillAmount()
	{
		return bilam;
	}
	public void addCharges(double amount)
	{
		bilam+=amount;
	}
	public String toString()
	{
		return pid;
	}
	public int compareTo(Patient p3)
	{
		return Integer.compare(p3.sesc,this.sesc);
	}
}

public class HospitalPatient 
{
	public static void main(String[] str)
	{
		Scanner s=new Scanner(System.in);
		double tbam=0;
		int max=0;
		Patient[] p=new Patient[6];
		p[0]=new Patient("H101","Meera","Dengue",6,4500.0);
		p[1]=new Patient("H102","Suresh","Fracture",4,8000.0);
		p[2]=new Patient("H103","Anitha","Cardiac",9,15500.0);
		p[3]=new Patient("H104","Ravi","Dengue",5,4000.0);
		p[4]=new Patient("H105","Kavya","Pneumonia",7,9800.0);
		p[5]=new Patient("H106","Imran","Fracture",3,6000.0);
		ArrayList<Patient> PatientList=new ArrayList<Patient>();
		for(int i=0;i<6;i++)
		{
			PatientList.add(p[i]);
			
		}
		System.out.println("Patient List:");
		System.out.println(PatientList+"\n");
		Collections.sort(PatientList);
		System.out.println("Sorted by Severity (Descending):");
		System.out.println(PatientList+"\n");
		ListIterator<Patient> itr=PatientList.listIterator();
		while(itr.hasNext())
		{
			Patient p1=itr.next();
			System.out.println(p1.pid+" "+p1.na+" "+p1.dis+" Severity = "+p1.sesc);
			tbam+=p1.bilam;
			if(max<p1.sesc)
			{
				max=p1.sesc;
			}
		}
		System.out.println("\nTotal Bill Amount is : "+tbam);
		ListIterator<Patient> itr1=PatientList.listIterator();
		while(itr1.hasNext())
		{
			Patient p1=itr1.next();
			if(max==p1.sesc)
			{
				System.out.println("Most Critical Patient = "+p1.na+" (Severity = "+p1.sesc+")");
			}
		}
		System.out.println();
		while(itr.hasPrevious())
		{
			Patient p1=itr.previous();
			System.out.println(p1.pid+" "+p1.na+" "+p1.dis+" Severity = "+p1.sesc);
		}
		System.out.print("\n\nEnter a Disease name : ");
		String sec=s.next();
		System.out.println("Patients with Disease : "+sec);
		while(itr.hasNext())
		{
			Patient p1=itr.next();
			if(p1.dis.equals(sec))
			{
				System.out.println(p1.pid+" - "+p1.na);
			}
		}
		System.out.print("Enter Patient ID to add charges : ");
		String seid=s.next();
		System.out.print("Enter extra charge amount : ");
		double at=s.nextDouble();
		ListIterator<Patient> itr2=PatientList.listIterator();
		while(itr2.hasNext())
		{
			Patient p1=itr2.next();
			if(p1.pid.equals(seid))
			{
				p1.addCharges(at);
				System.out.println("Updated Bill for "+p1.pid+" : "+p1.bilam+"\n");
				break;
			}
		}
		Map<String,Integer> dislist=new LinkedHashMap<>();
		ListIterator<Patient> itr3=PatientList.listIterator();
		while(itr3.hasNext())
		{
			int b=0;
			Patient p5=itr3.next();
			for (Map.Entry<String,Integer> entry : dislist.entrySet())
			{
				if((entry.getKey()).equals(p5.dis))
				{
					dislist.put(entry.getKey(),entry.getValue()+1);
					b=1;
				}
			}
			if(b==0)
			{
				dislist.put(p5.dis,1);
			}
		}
		System.out.println("Disease-wise Patient Count:");
		for(Map.Entry<String, Integer> ent : dislist.entrySet())
		{
			System.out.println(ent.getKey()+"->"+ent.getValue());
		}
	}
}