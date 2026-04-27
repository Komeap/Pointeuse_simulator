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


}
