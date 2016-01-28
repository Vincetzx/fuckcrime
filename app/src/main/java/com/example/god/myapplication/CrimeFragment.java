package com.example.god.myapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by god on 2016/1/19.
 */
public class CrimeFragment extends Fragment {
    public static final String EXTRA_CRIME_ID ="vczx";
    public static final int REQUEST_CODE=0;
    private static final int REQUEST_PHOTO=1;
    private static final String TAG="CrimeFragment";
    private static final int REQUEST_CONTACE=2;

    private Button mSuspectButton;
     Crime mCrime;
     EditText mCrimeTitle;
     Button mCrimeDate;
     CheckBox mCrimeSolved;
    private ImageButton mImageButton;
    private ImageView mPhotoView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
     UUID crimeId=(UUID)getArguments().getSerializable(EXTRA_CRIME_ID);
        mCrime=CrimeLab.getCrimeLab(getActivity()).getCrime(crimeId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.home:
                if(NavUtils.getParentActivityName(getActivity())!=null)
                {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            case R.id.menu_item_delete_crime:
                CrimeLab.getCrimeLab(getActivity()).deleteCrime(mCrime);
                NavUtils.navigateUpFromSameTask(getActivity());
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static CrimeFragment newInstance(UUID uuid)
    {
        Bundle args=new Bundle();
        args.putSerializable(EXTRA_CRIME_ID, uuid);
        CrimeFragment fragment=new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.crime_list_item_context, menu);
    }



    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.getCrimeLab(getActivity()).saveCrimes();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_crime,container,false);
        if(NavUtils.getParentActivityName(getActivity())!=null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mSuspectButton=(Button)view.findViewById(R.id.crime_suspectButton);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent,REQUEST_CONTACE);
            }
        });
        if(mCrime.getSuspect()!=null)
        {
            mSuspectButton.setText(mCrime.getSuspect());
        }
        Button reportButton=(Button)view.findViewById(R.id.crime_reportButton);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                intent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.crime_report_subject));
                intent=Intent.createChooser(intent,"Send crime report");
                startActivity(intent);
            }
        });
        mPhotoView=(ImageView)view.findViewById(R.id.crime_imageView);
        mImageButton=(ImageButton)view.findViewById(R.id.crime_imageButton);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),CrimeCameraActivity.class);
                startActivityForResult(intent,REQUEST_PHOTO);
            }
        });

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Photo p=mCrime.getPhoto();
                if(p==null)
                {
                    return;
                }
                FragmentManager fm=getActivity().getSupportFragmentManager();
                String path=getActivity().getFileStreamPath(p.getFileName()).getAbsolutePath();
                ImageFragment.newIntance(path).show(fm,"image");
            }
        });
        mCrimeTitle=(EditText)view.findViewById(R.id.crime_title);
        mCrimeDate=(Button)view.findViewById(R.id.crime_date);
        mCrimeSolved=(CheckBox)view.findViewById(R.id.crime_solved);//这里是空指针，为什么呢，难道是因为mCrime没有初始化？但是已经绑定了啊
        mCrimeTitle.setText(mCrime.getTitle());
        mCrimeDate.setText(mCrime.getDate().toString() + "");

        mCrimeSolved.setChecked(mCrime.isSolved());


        mCrimeTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mCrimeSolved.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });
        mCrimeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm=getActivity().getSupportFragmentManager();
                DatePickerFragment dp=DatePickerFragment.newInstance(mCrime.getDate());
                dp.setTargetFragment(CrimeFragment.this,REQUEST_CODE);
                dp.show(fm,"Date");
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!= Activity.RESULT_OK)
        {
            return;
        }
        if(requestCode==REQUEST_CODE)
        {
            Date date=(Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            mCrimeDate.setText(mCrime.getDate().toString());
        }
        else if(requestCode==REQUEST_PHOTO)
        {
            String fileName=data.getStringExtra(CrimeCameraFragment.EXTRA_PHOTO_FILENAME);
            if(fileName!=null)
            {
                Photo p=new Photo(fileName);
                mCrime.setPhoto(p);
                showPhoto();
                Log.i(TAG, "onActivityResult filename: " + mCrime.getTitle()+"has a photo");
            }
        }
    }

    private void showPhoto()
    {
        Photo p=mCrime.getPhoto();
        BitmapDrawable b=null;
        if(p!=null)
        {
            String path=getActivity().getFileStreamPath(p.getFileName()).getAbsolutePath();
            b=PictureUtils.getScaledDrawable(getActivity(),path);
        }
        mPhotoView.setImageDrawable(b);
    }

    @Override
    public void onStart() {
        super.onStart();
        showPhoto();
    }

    private String getCrimeReport()
    {
        String solvedString=null;
        if(mCrime.isSolved())
        {
            solvedString=getString(R.string.crime_report_solved);
        }
        else
        {
            solvedString=getString(R.string.crime_report_unsolved);
        }
        String suspect=mCrime.getSuspect();
        if(suspect==null)
        {
            suspect=getString(R.string.crime_report_no_suspect);
        }
        else
        {
            suspect=getString(R.string.crime_report_suspect);
        }
        String dateFormat="EEE,MMM dd";
        String dateString= android.text.format.DateFormat.format(dateFormat,mCrime.getDate()).toString();
        String report=getString(R.string.crime_report,mCrime.getTitle(),dateString,solvedString,suspect);
        return report;

    }
}
