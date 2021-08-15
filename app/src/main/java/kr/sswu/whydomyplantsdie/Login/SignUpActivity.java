package kr.sswu.whydomyplantsdie.Login;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private EditText editTextPasswordConfirm;
    private Button buttonJoin;
    private ImageView close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = (EditText) findViewById(R.id.edt_email);
        editTextPassword = (EditText) findViewById(R.id.edt_passWord);
        editTextPasswordConfirm = (EditText) findViewById(R.id.edt_check_password);
        buttonJoin = (Button) findViewById(R.id.btn_join);
        close = (ImageView) findViewById(R.id.iv_close);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInputValid(
                        editTextEmail.getText().toString(),
                        editTextPassword.getText().toString(),
                        editTextPasswordConfirm.getText().toString())) {
                    createUser(editTextEmail.getText().toString(), editTextPassword.getText().toString());
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = firebaseAuth.getCurrentUser();
    }

    private Boolean isInputValid(String email, String password, String password2) {

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(SignUpActivity.this, "이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(SignUpActivity.this, "비밀번호는 6자리 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!(password.equals(password2))) {
            Toast.makeText(SignUpActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void createUser(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 회원가입 성공시
                            Toast.makeText(SignUpActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, "이미 가입된 이메일입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
