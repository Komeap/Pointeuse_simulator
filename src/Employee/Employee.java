package Employee;
import Entreprise.Department;
import Check.Check;
import Planning.Planning;

import java.util.UUID;

public class Employee {

	private UUID employeeId;
	private String firstName;
	private String lastName;
	private Department department;
	private Planning planning;
	private Check check;

	public Employee(UUID newEmployeeId, String newFirstName, String newLastName, Department newDepartment, Planning newPlanning, Check newCheck) {
		employeeId = newEmployeeId;
		firstName = newFirstName;
		lastName = newLastName;
		department = newDepartment;
		planning = newPlanning;
		check = newCheck;
	}

	public void setEmployeeId(UUID employeeId) {
		this.employeeId = employeeId;
	}

	public UUID getEmployeeId() {
		return employeeId;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getLastName() {
		return lastName;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Department getDepartment() {
		return department;
	}

	public void setPlanning(Planning planning) {
		this.planning = planning;
	}

	public Planning getPlanning() {
		return planning;
	}

	public void setCheck(Check check) {
		this.check = check;
	}

	public Check getCheck() {
		return check;
	}


}
