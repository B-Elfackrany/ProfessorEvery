# Firebase Setup Guide for Professor Every

## Issue: Permission Denied Error During Sign Up

The permission denied error occurs because Firestore security rules are not properly configured to allow user registration and data access.

## Solution: Update Firestore Security Rules

### Current Problem
The default Firestore security rules deny all read/write operations. This causes the `PERMISSION_DENIED` error when trying to create user documents after successful authentication.

### Required Firestore Security Rules

Go to your Firebase Console > Firestore Database > Rules and replace the current rules with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow users to read and write their own user document
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow authenticated users to read and write posts
    match /posts/{postId} {
      allow read, write: if request.auth != null;
    }
    
    // Allow authenticated users to read and write comments
    match /posts/{postId}/comments/{commentId} {
      allow read, write: if request.auth != null;
    }
    
    // Allow authenticated users to read university data (if needed)
    match /universities/{universityId} {
      allow read: if request.auth != null;
    }
  }
}
```

### Alternative Temporary Rules (For Development Only)

If you want to quickly test without restrictions (NOT recommended for production):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## Firebase Authentication Setup

Ensure that Email/Password authentication is enabled:

1. Go to Firebase Console > Authentication > Sign-in method
2. Enable "Email/Password"
3. Optionally enable "Email link (passwordless sign-in)" if needed

## Firebase Project Configuration

Verify your `google-services.json` file is correctly placed in the `app/` directory and contains the correct project configuration.

## Testing the Fix

1. Update the Firestore security rules as shown above
2. Clean and rebuild your project
3. Try signing up with a valid educational email (.edu, .ac.kr, .edu.kr)
4. The user should be successfully created and stored in Firestore

## Additional Improvements Made

1. **Internationalization**: Added English (default) and Korean language support
2. **Language Toggle**: Added language toggle buttons on login and signup screens
3. **String Resources**: Replaced all hardcoded strings with proper string resources
4. **Language Helper**: Created a utility class for consistent language management

## Language Toggle Usage

- The app defaults to English
- Users can toggle between English and Korean using the language toggle button
- Language preference is persisted across app sessions
- All screens will reflect the selected language 