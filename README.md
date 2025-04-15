# Open vs Secure Mode

StreamIT operates in two modes: **Open** and **Secure**, which define how incoming requests are authenticated and validated based on context and intent.

---

## 🎯 Why This Separation Exists

The design is focused on keeping your content **private by default**, especially in home network setups.  
At the same time, it allows for secure access if you choose to expose the system externally.

This setup:
- Helps avoid unintentional sharing of your media library.
- Keeps sensitive actions behind authentication.
- Puts full responsibility and control in the hands of the user.

> ⚠️ Misconfigured or intentionally open systems **may expose** the library.  
> The software does not restrict access — it’s up to you how it’s deployed.

---

## 🔓 Open Mode

- Intended for **trusted local networks**.
- No authentication or validation is performed.
- Everything is accessible by any device on the network.

```
Local Network
+-------------+      No Auth      +---------------+
|  Client App | --------------->  | StreamIT API  |
+-------------+                   +---------------+
```

---

## 🔐 Secure Mode

- Designed for **untrusted or external networks**.
- Requires authentication and/or validation for most requests.
- Prevents anonymous access to sensitive or write-level actions.

```
Remote Network
+-------------+      JWT Token    +---------------+
|  Client App | --------------->  | StreamIT API  |
+-------------+                   +---------------+
                                     |
                                Validates JWT
```

---

## 🔄 Separation Logic

- Both the **API** and the **Streaming Server** validate requests separately.
- The streaming component is a **separate app** and is not covered here.
- Authentication is handled **locally**, without external dependencies.

---

## 🛡️ Types of Validation

### ✅ Soft Validation

Applies to **non-sensitive information** — data that has little or no value outside your system.

Used for operations like:
- Fetching media metadata
- Browsing the library
- Reading status from existing devices

Soft validation checks:
- If a JWT is present and valid
- If the token’s owner exists in the current system

```
Example: Fetching library items
If token is valid and user exists → Allow
If user does not exist → Reject
```

---

### ❌ Strict Validation (Hard)

Applies to **sensitive operations**, such as authentication, access control, or device registration.

Used for:
- Logging in and obtaining JWT tokens
- Delegating authentication
- Creating or linking new devices
- Changing user or library settings

Strict validation:
- Requires a valid JWT token
- Rejects all unauthenticated requests with **401 Unauthorized**

```
Example: Creating a new device link
If no token → Reject (401)
If invalid token → Reject
```

---

Let me know:
- Want a config snippet for how to toggle between modes?
- Should I include streaming server examples in a separate section later?
