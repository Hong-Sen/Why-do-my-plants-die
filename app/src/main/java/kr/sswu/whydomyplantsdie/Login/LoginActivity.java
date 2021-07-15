package kr.sswu.whydomyplantsdie.Login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import kr.sswu.whydomyplantsdie.MainActivity;
import kr.sswu.whydomyplantsdie.R;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth firebaseAuth = null;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager callbackManager;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button btnLogIn;
    private Button btnSignUp;
    private ImageView btnGoogleSignIn;
    private LoginButton btnFacebookSignIn;
    private ImageView btnFakeFacebook;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editTextEmail = findViewById(R.id.edt_email);
        editTextPassword = findViewById(R.id.edt_password);
        btnLogIn = findViewById(R.id.btn_login);
        btnSignUp = findViewById(R.id.btn_createAccount);
        btnGoogleSignIn = findViewById(R.id.btn_googleSignIn);
        btnFacebookSignIn = findViewById(R.id.btn_facebookSignIn);
        btnFakeFacebook = findViewById(R.id.btn_fake_facebook);
        callbackManager = CallbackManager.Factory.create();

        firebaseAuth = FirebaseAuth.getInstance();

        btnFakeFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFacebookSignIn.performClick();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editTextEmail.getText().toString().equals("") && !editTextPassword.getText().toString().equals("")){
                    loginUser(editTextEmail.getText().toString(),editTextPassword.getText().toString());
                }
                else{
                    Toast.makeText(LoginActivity.this, "계정과 비밀번호를 입력하세요.", Toast.LENGTH_LONG).show();
                }
            }
        });

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                updateUI(user);
            }
        };

        //로그인 성공후 재시작 로그인 화면 건너뛰기
        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(getApplication(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        btnFacebookSignIn.setReadPermissions("email", "public_profile");
        btnFacebookSignIn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("facebook login", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("facebook login", "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("facebook login", "facebook:onError", error);
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token){
        Log.d("FACEBOOK Login", "handleFacebookAccessToken:" + token.getToken());

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("FACEBOOK Login", "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FACEBOOK Login", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    public void loginUser(String email, String password){
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //로그인 성공
                            Toast.makeText(LoginActivity.this,"로그인 성공", Toast.LENGTH_SHORT).show();
                            firebaseAuth.addAuthStateListener(firebaseAuthListener);
                        }
                        else{
                            Toast.makeText(LoginActivity.this,"로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuthListener != null){
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }

    // google sign in
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            task.addOnSuccessListener(googleSignInAccount -> {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                    Log.d("LoginActivity", e.getLocalizedMessage());
                    Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Snackbar.make(findViewById(R.id.layout_login), "Authentication Successed.", Snackbar.LENGTH_SHORT).show();
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Snackbar.make(findViewById(R.id.layout_login), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    //로그인 성공시 main화면으로
    private void updateUI(FirebaseUser user) { //update ui code here
        if (user != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}