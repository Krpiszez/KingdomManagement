# Security

## Roles

The application starts with three roles:

- `ADMIN`: full application access.
- `OFFICER`: operational access, including data imports and permitted management actions.
- `KINGDOM_MEMBER`: authenticated read access.

`VISITOR` is intentionally not implemented yet. Public access should be introduced later through an explicit allow-list of safe, public statistics endpoints.

## Initial administrator

Set these environment variables before the first startup:

```text
APP_INITIAL_ADMIN_USERNAME=admin
APP_INITIAL_ADMIN_PASSWORD=<strong-password>
```

The password is encoded with BCrypt before it is stored.

The initializer only creates the account when the username does not already exist. It never overwrites an existing password.

## Authorization

Authorization is enforced by Spring Security on the server. Hiding a button in Thymeleaf is only a UI convenience; it is not the security boundary.

Current protected areas:

- `/admin/**` -> `ADMIN`
- `/characters/import` -> `ADMIN`, `OFFICER`
- `/ame/import` -> `ADMIN`, `OFFICER`
- `/credit-fort/import` -> `ADMIN`, `OFFICER`
- Governor/performance viewing -> all three authenticated roles

## Production notes

Use environment variables or a secrets manager for database and administrator credentials. Do not commit real passwords to source control.
