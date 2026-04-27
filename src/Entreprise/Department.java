package Entreprise;
import Employee.Employee;
import java.util.List;
import java.util.ArrayList;

public class Department {

	private String depName; //name of departement

	private List <Employee> employeeList; //list of employees who are in this departement

	//constructor of Departement
	public Department(String sDepName)
	{
		this.depName = sDepName;

		this.employeeList = new ArrayList<>();
	}

	//- - - ATTRIBUTE - - -
		//attribute depName
	public String getDepartement(){ return this.depName;} //allows access to the depName value

	public void setDepartement(String sDepName){ this.depName = sDepName;} //allows edit to the depName value

		//attribute employeeList
	public List <Employee> getEmployeeList(){ return new ArrayList<>(employeeList);} //allows access to the employeeList list

	//allows add a new employee in the employeeList list
	public void addEmployee(Employee employee)
	{
		if (employee == null)
			return;
		for(int loop = 0; loop < employeeList.size(); loop++)
		{
			if(employee == employeeList.get(loop))
				return;
		}
		this.employeeList.add(employee);
	}

	//allows remove a employee in the employeeList list
	public void removeEmployee(Employee employee)
	{
		this.employeeList.remove(employee);
	}
}

