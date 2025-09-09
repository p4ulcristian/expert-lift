
# project-template

### Overview

The <strong>project-template</strong> is our Clojure/ClojureScript web application
template based on the Monoset kit-framework, the x5 web application engine
and the project-kit web application module-kit.

### How to use this repository as a template?

- (A)
When you create a new repository on `github.com` and you are added to this
template as a contributor, you can find it in the 'Repository template'
select on the top of the the `Create a new repository` page.

- (B)
Go to the template's repository page on `github.com`, and click the `Use this template`
button (you find it above the file list).
Select the `Create a new repository` option.

### How to clone this repository from GitHub?

```
git clone git@github.com:monotech-hq/project-template.git
```

### How to install node modules?

```
npm install
```

### How to start site development?

```
clj -X:site.dev
```
After the build is ready, open the browser on: `localhost:3000`

### How to start app development?

```
clj -X:app.dev
```

After the build is ready, open the browser on: `localhost:3000/app`

### How to compile a JAR executable version?

```
clj -X:prod
```

To run the JAR file use this: `java -jar my-project-0-0-1.jar 3000`

To connect to Clojure Nrepl with Atom + Chlorine use port: `5555`

To connect Shadow-CLJS Nrepl with Atom + Chlorine use build: `app`


ENV VARIABLES: 

export IRONRAINBOW_DATABASE_NAME=""
export IRONRAINBOW_DATABASE_USERNAME=""
export IRONRAINBOW_DATABASE_PASSWORD=""
export IRONRAINBOW_DATABASE_SERVER=""

