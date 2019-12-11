# microprofile-jwt
A Microprofile JWT RBAC sample which includes endpoint to get token and a sample secure endpoint

Provide implementation of UserModelProvider, a sample provider is available which returns a null UserModel, which results an error when calling /oauth/token endpoint "User not found".

Integrate DynamoDB, postgresql, MySQL or whatever you like to return UserModel and a token will be returned if password matches.

`Build GraalVM native image`
mvn clean package -Pnative

`Docker`
mvn clean package -Pdocker
