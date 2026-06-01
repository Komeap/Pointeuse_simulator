package Entreprise;

import Employee.Employee;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Department implements Serializable{
	private static final long serialVersionUID = 1L;
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

	@Override
	public String toString() {
		return this.depName; // Permet à JavaFX d'afficher le nom du département
	}

}

