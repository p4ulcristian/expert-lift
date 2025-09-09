```
███████╗███████╗██████╗  ██████╗ 
╚══███╔╝██╔════╝██╔══██╗██╔═══██╗
  ███╔╝ █████╗  ██████╔╝██║   ██║
 ███╔╝  ██╔══╝  ██╔══██╗██║   ██║
███████╗███████╗██║  ██║╚██████╔╝
╚══════╝╚══════╝╚═╝  ╚═╝ ╚═════╝ 
```

**Zero** is a dynamically crafted framework designed to build full-stack modules following a set of straightforward rules.  

---

## **Project Structure**
 
### `/blocks`  
Contains reusable components and utilities such as inputs, views, parsers, converters, or other frequently used functions.  
- **`/backend`**: Backend-specific blocks.  
- **`/common`**: Shared blocks used across both backend and frontend.  
- **`/frontend`**: Frontend-specific blocks.  

### `/blueprints`  
Blueprints represent more complex modules that can optionally integrate backend and frontend functionality. Examples include features like an item lister.  

### `/pages`  
Pages represent routes or groups of routes. 

### `/zero`  
- Framework behind the modules. Preparing a world.
- **`/backend`**
  - Starting the server.  
  - Compiling the project.  
  - Connecting to the database.  
  - Setting up re-frame events.  
  - Adding Pathom to the system.  

- **`/frontend`**: 
   - Setting up re-frame events.  
   - **`/builds`**: Frontend builds. 
