# MongoDB Atlas Setup for Draftly Team

## Step 1: Create MongoDB Atlas Account (You - 5 mins)

1. Go to https://www.mongodb.com/cloud/atlas
2. Click **"Sign Up for Free"**
3. Create account (email + password)
4. Verify email
5. Accept terms & create organization
---

## Step 2: Create a Cluster (You - 3 mins)

1. Click **"Create a Deployment"**
2. Select **"M0 Free"** tier (plenty for dev/demo)
3. Cloud Provider: **AWS** (or your preference)
4. Region: **Pick closest to your team**
5. Cluster Name: **draftly-dev**
6. Click **"Create Deployment"**
7. Wait 2-3 minutes for cluster to spin up ✅

---

## Step 3: Create Database User (You - 2 mins)

1. In left sidebar, click **"Database Access"**
2. Click **"Add New Database User"**
3. Username: `draftly_dev`
4. Password: `GenerateSecurePassword` (copy this!)
5. Built-in Role: **"Atlas Admin"**
6. Click **"Add User"**
7. **Save username + password somewhere safe** (you'll share with team)

---

## Step 4: Allow Your IP (You - 1 min)

1. In left sidebar, click **"Network Access"**
2. Click **"Add IP Address"**
3. Click **"Allow Access from Anywhere"** (for team access)
4. Click **"Confirm"**

---

## Step 5: Get Connection String (You - 2 mins)

1. In left sidebar, click **"Databases"**
2. Click your cluster **"draftly-dev"**
3. Click **"Connect"** button
4. Select **"Drivers"** → **"Java"**
5. Copy the connection string (looks like below)
6. Replace `<username>` and `<password>` with your creds from Step 3

**Example connection string:**
```
mongodb+srv://draftly_dev:YOUR_PASSWORD_HERE@draftly-dev.xxxxx.mongodb.net/draftly?retryWrites=true&w=majority
```
---

## Step 6: Update Backend Config (You - 2 mins)

Edit `src/main/resources/application.properties`:

```properties
spring.application.name=draftly

# MongoDB Atlas Configuration
spring.data.mongodb.uri=mongodb+srv://draftly_dev:YOUR_PASSWORD_HERE@draftly-dev.xxxxx.mongodb.net/draftly?retryWrites=true&w=majority
spring.data.mongodb.database=draftly

# Server Configuration
server.port=8080
```

**Test it:** 
```bash
./gradlew bootRun
```
If successful, you'll see: `Started DraftlyApplication in X.XXX seconds`

---
## Troubleshooting

### Error: "Authentication failed"
- Check username/password in connection string (Step 3)
- Verify IP whitelist includes their IP (Step 4)

### Error: "Cannot connect to server"
- Check internet connection
- Verify "Allow Access from Anywhere" is enabled (Step 4)
- Try pinging the cluster in MongoDB Atlas console

### Error: "Database draftly not found"
- This is normal; MongoDB creates it automatically on first write
- Just make sure the database name in connection string is `draftly`
---
