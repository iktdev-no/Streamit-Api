## What is "secure" and "open"
### "Open"
Open is meant for environments where there is no need to authenticate and validate in order to perform any operations
<br />
In this case open is meant for local network which is intended to be secure

### "Secure"
Secure is meant for connections where there is no security (other than firewall etc) where the service might be accessed by anyone
Here secure controllers should and must perform a validation on any post request

### The idea with the separation
In order to secure the content and protect the systems data. In order to attempt to mitigate this issue, both the stream server and the api will both do their own hard and soft authentication/validation.
Since StreamIT is only intended for home use, the design is intended to prevent a new validation from external sources

- What does soft validation mean
  - Soft validation means that the system will only check if the JWT is valid before performing a secure call. Example of this is just fetching data. In any instance that data is uploaded it will also check the owner of the token. If the owner does not exist within the current running system, any call will be rejected
- What does hard validation mean
  - Hard validation means that the system will not permit the call and return a 401, like the streaming server will if there is no JWT present