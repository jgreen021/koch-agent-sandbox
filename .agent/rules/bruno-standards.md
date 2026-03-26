---
trigger: always_on
---

# Rule: Bruno Collection Standard (OpenCollection YAML)

## Overview
All REST API verification must use the Bruno OpenCollection YAML format. This ensures Git-friendliness and high-performance parsing by AI agents.

## Directory Structure
- Store collections in `bruno/` at the project root.
- Environment variables must be in `.env` files within the collection folder.

# Bruno OpenCollection YAML Structure Reference
## Indentation Rules
- Use 2-space indentation consistently.
- YAML is indentation-sensitive — child keys must be indented further than their parent.
- For block scalars (|-), the content on the following lines must be indented further than the key that declares it.

## Top-Level Structure (each at column 0)
```code
info:        # Request metadata
http:        # HTTP request configuration
runtime:     # Scripts and assertions
settings:    # Request settings
docs:        # Documentation (markdown)
```
### **info** (indent: 2)
```code
info:
  name: Create User       # string, required
  type: http               # "http" or "folder"
  seq: 1                   # number, sort order
  tags:                    # optional array
    - smoke
    - regression
```
### **http** (indent: 2, children at 4)
```code
http:
  method: POST                          # GET|POST|PUT|PATCH|DELETE|OPTIONS|HEAD
  url: https://api.example.com/users
  params:                               # array of objects
    - name: filter
      value: active
      type: query                       # "query" or "path"
  headers:                              # array of objects
    - name: Content-Type
      value: application/json
      disabled: false                   # optional boolean
  body:                                 # indent: 4
    type: json                          # json|text|xml|form-urlencoded|multipart-form|graphql
    data: |-                            # indent: 6 for block content
      {
        "name": "John Doe",
        "email": "john@example.com"
      }
  auth:                                 # at indent 4 (sibling of body, NOT inside body)
    type: bearer
    token: "{{token}}"
```
## Critical: **body.data** block scalar indentation
```code
body:                    ← indent 4
    type: json             ← indent 6
    data: |-               ← indent 6
      {                    ← indent 8 (MUST be deeper than "data")
        "key": "value"     ← indent 10
      }                    ← indent 8
```
### **auth** examples (at same level as method, url, body)
```code
auth: inherit            # or auth: none
  # OR
  auth:
    type: basic
    username: admin
    password: secret
  # OR
  auth:
    type: apikey
    key: x-api-key
    value: "{{api-key}}"
    placement: header
```
### **runtime**  (indent: 2)
```code
runtime:
  scripts:
    - type: before-request           # before-request | after-response | tests
      code: |-
        console.log('before request');
        req.setHeader("X-Timestamp", Date.now());
    - type: tests
      code: |-
        test("should return 200", function() {
          expect(res.status).to.equal(200);
        });
  assertions:
    - expression: res.status
      operator: eq
      value: "200"
    - expression: res.body.name
      operator: isString
```
### **settings**  (indent: 2)
```code
settings:
  encodeUrl: true          # boolean
  timeout: 0               # milliseconds, 0 = no timeout
  followRedirects: true    # boolean
  maxRedirects: 5          # number
```
### **docs** (indent: 0, block scalar)
```code
docs: |-
  # API Documentation
  Markdown content here.
```

### **Collection File (opencollection.yml)**
Use this structure for every new API request generation:
```yaml
opencollection: "1.0.0"
info:
  name: My API Collection
  version: "1.0.0"
bundled: true
config:
  environments:
    - name: local
      variables:
        - name: baseUrl
          value: http://localhost:8080
          enabled: true
items:
  - name: Create User
    type: http
    info:
      name: Create User
      seq: 1
    http:
      method: POST
      url: "{{baseUrl}}/users"
      body:
        type: json
        data: |-
          {
            "name": "John Doe"
          }
      auth: none
```