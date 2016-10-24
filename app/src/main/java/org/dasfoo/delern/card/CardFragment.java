package org.dasfoo.delern.card;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.dasfoo.delern.R;
import org.dasfoo.delern.controller.FirebaseController;
import org.dasfoo.delern.models.Card;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CardFragment extends Fragment {
    private static final String FB_REFERENCE = "reference";

    private FirebaseController firebaseController = FirebaseController.getInstance();
    private static final String Tag = CardFragment.class.getSimpleName();

    private Button mKnowButton;
    private Button mMemorizeButton;
    private Button mRepeatButton;
    private Button mNextButton;
    private TextView mTextView;
    private Iterator<Card> mCardIterator;
    private Card mCurrentCard;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.to_know_button:
                    showBackSide();
                    mMemorizeButton.setVisibility(View.VISIBLE);
                    break;
                case R.id.to_memorize_button:
                    // TODO: Add time parameters and memorization logic
                    break;
                case R.id.to_repeat_button:
                    showBackSide();
                    mMemorizeButton.setVisibility(View.INVISIBLE);
                    break;
                case R.id.next_button:
                    if (mCardIterator.hasNext()) {
                        mCurrentCard = mCardIterator.next();
                        showFrontSide();
                    } else {
                        getFragmentManager().popBackStack();
                    }
                    break;
                default:
                    Log.v("CardFragment", "Button is not implemented yet.");
                    break;
            }
        }
    };

    public CardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fbReference reference to firebase dataset.
     * @return A new instance of fragment CardFragment.
     */
    public static CardFragment newInstance(final String fbReference) {
        CardFragment fragment = new CardFragment();
        Bundle args = new Bundle();
        args.putString(FB_REFERENCE, fbReference);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String dbReference = getArguments().getString(FB_REFERENCE);
            // Init Firebase to get cards
            assert dbReference != null;

            DatabaseReference fdReference = firebaseController
                    .getCardsRefFromDesktopUrl(dbReference);
            // Attach a listener to read the cards. This function will be called anytime
            // new data is added to our database reference.
            // TODO(ksheremet): Refactor
            fdReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Card> cards = new ArrayList<>();
                    for (DataSnapshot cardSnapshot : dataSnapshot.getChildren()) {
                        Card card = cardSnapshot.getValue(Card.class);
                        card.setUid(cardSnapshot.getKey());
                        cards.add(card);
                    }
                    try {
                        mCardIterator = cards.iterator();
                        mCurrentCard = mCardIterator.next();
                        showFrontSide();
                    } catch (NoSuchElementException e) {
                        getFragmentManager().popBackStack();
                    }
                }

                // TODO(ksheremet): Implementation on error
                @Override
                public void onCancelled(DatabaseError databaseError) {
                // Not implemented yet.
                }
            });

        } else {
            // If no parameters, return to previous state
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater,final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_card, container, false);
        mKnowButton = (Button) view.findViewById(R.id.to_know_button);
        mKnowButton.setOnClickListener(onClickListener);
        mMemorizeButton = (Button) view.findViewById(R.id.to_memorize_button);
        mMemorizeButton.setOnClickListener(onClickListener);
        mRepeatButton = (Button) view.findViewById(R.id.to_repeat_button);
        mRepeatButton.setOnClickListener(onClickListener);
        mNextButton = (Button) view.findViewById(R.id.next_button);
        mNextButton.setOnClickListener(onClickListener);
        mTextView = (TextView) view.findViewById(R.id.textCardView);
        return view;
    }

    @Override
    public final void onAttach(final Context context) {
        super.onAttach(context);
        if (!(context instanceof OnFragmentInteractionListener)) {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public final void onDetach() {
        super.onDetach();
    }

    /**
     * Shows front side of the current card and appropriate buttons
     */
    private void showFrontSide() {
        mTextView.setText(mCurrentCard.getFrontSide());
        mMemorizeButton.setVisibility(View.INVISIBLE);
        mRepeatButton.setVisibility(View.VISIBLE);
        mKnowButton.setVisibility(View.VISIBLE);
        mNextButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Shows back side of current card and appropriate buttons.
     */
    private void showBackSide() {
        mTextView.setText(mCurrentCard.getBackSide());
        mNextButton.setVisibility(View.VISIBLE);
        mRepeatButton.setVisibility(View.INVISIBLE);
        mKnowButton.setVisibility(View.INVISIBLE);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}