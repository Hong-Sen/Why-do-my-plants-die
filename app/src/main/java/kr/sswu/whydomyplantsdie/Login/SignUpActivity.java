package kr.sswu.whydomyplantsdie.Login;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import kr.sswu.whydomyplantsdie.R;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextName;
    private Button buttonJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);

        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = (EditText) findViewById(R.id.edt_email);
        editTextPassword = (EditText) findViewById(R.id.edt_passWord);
        editTextName = (EditText) findViewById(R.id.edt_name);
        buttonJoin = (Button) findViewById(R.id.btn_join);

        buttonJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editTextEmail.getText().toString().equals("") && !editTextPassword.getText().toString().equals("")){
                    createUser(editTextEmail.getText().toString(), editTextPassword.getText().toString(), editTextName.getText().toString());
                }
                else{
                    Toast.makeText(SignUpActivity.this, "계정과 비밀번호를 입력하세요.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = firebaseAuth.getCurrentUser();
    }

    private void createUser(String email, String password, String name) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 회원가입 성공시
                            Toast.makeText(SignUpActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            if(email.indexOf('@') == -1 || email.indexOf("com") == -1){
                                Toast.makeText(SignUpActivity.this, "이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
                            }
                            else if(password.length() < 6){
                                Toast.makeText(SignUpActivity.this, "비밀번호는 6자리 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(SignUpActivity.this, "이미 존재하는 계정입니다.", Toast.LENGTH_SHORT).show();
                            }
                            Log.d("not success login", "createUserWithEmail:failure", task.getException());
                        }
                    }
                });
    }
}
