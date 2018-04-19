

# REST Examples

## Start the process

POST 

    http://localhost:8080/kie-server/services/rest/server/containers/order-management_1.0-SNAPSHOT/processes/OrderManagement.OrderManagement/instances

Payload

    {
    	"orderInfo": 
    	{
    		"com.example.OrderInfo": 
    		{
    			"item": "Lenovo Thinkpat T470s",
    			"urgency": "Low"
    		}
    	}
    }

Response: process id

## Find Tasks Assigned As Potential Owner

GET 

    http://localhost:8080/kie-server/services/rest/server/queries/tasks/instances/pot-owners?page=0&pageSize=20&sort=&sortOrder=true

Response: list of task

## Claim Task

PUT 

    http://localhost:8080/kie-server/services/rest/server/containers/order-management_1.0-SNAPSHOT/tasks/3/states/claimed 

## Start Task

PUT 

    http://localhost:8080/kie-server/services/rest/server/containers/order-management_1.0-SNAPSHOT/tasks/3/states/started


## Complete Task

PUT /kie-server/services/rest/server/containers/order-management_1.0-SNAPSHOT/tasks/12/states/completed 

    {
    	"orderInfo": {
    		"com.example.OrderInfo": {
    			"orderId": 7,
    			"item": "Monitor Dell U2412M",
    			"category": "basic",
    			"urgency": "low",
    			"price": 0.0,
    			"managerApproval": null,
    			"rejectionReason": null
    		}
    	},
    
    	"supplierInfo": {
    		"com.example.SupplierInfo": {
    			"user": "donato",
    			"offer": 0.0,
    			"deliveryDate": null,
    			"selected": false
    		}
    	}
    }



