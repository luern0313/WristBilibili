package cn.luern0313.wristbilibili.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

import androidx.cardview.widget.CardView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.LotteryModel;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;
import cn.luern0313.wristbilibili.util.MyApplication;

/**
 * 被 luern0313 创建于 2020/4/25.
 */
public class LotteryAdapter extends BaseExpandableListAdapter
{
    private final Context ctx;
    private final LayoutInflater inflater;

    private final LotteryListener lotteryListener;

    private final LotteryModel lotteryModel;
    private final ListView listView;

    public LotteryAdapter(LayoutInflater inflater, LotteryModel lotteryModel, ListView listView, LotteryListener lotteryListener)
    {
        this.ctx = MyApplication.getContext();
        this.inflater = inflater;
        this.lotteryModel = lotteryModel;
        this.listView = listView;
        this.lotteryListener = lotteryListener;
    }

    @Override
    public int getGroupCount()
    {
        return existGift(lotteryModel.getGiftFirstName()) + existGift(lotteryModel.getGiftSecondName()) +existGift(lotteryModel.getGiftThirdName());
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        if(groupPosition == 0 && lotteryModel.getGiftFirstResult() != null)
            return lotteryModel.getGiftFirstResult().size();
        else if(groupPosition == 1 && lotteryModel.getGiftSecondResult() != null)
            return lotteryModel.getGiftSecondResult().size();
        else if(groupPosition == 2 && lotteryModel.getGiftThirdResult() != null)
            return lotteryModel.getGiftThirdResult().size();
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        LotteryModel.LotteryGiftModel lotteryGiftModel;
        GiftViewHolder giftViewHolder;
        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.item_lottery_gift_gift, parent, false);
            giftViewHolder = new GiftViewHolder();
            giftViewHolder.card = convertView.findViewById(R.id.item_join_num_img);
            giftViewHolder.img = convertView.findViewById(R.id.item_lottery_gift_card_img);
            giftViewHolder.ranking = convertView.findViewById(R.id.item_lottery_gift_ranking);
            giftViewHolder.gift = convertView.findViewById(R.id.item_lottery_gift_gift);
            giftViewHolder.expand = convertView.findViewById(R.id.item_lottery_expand);
            convertView.setTag(giftViewHolder);
        }
        else
        {
            giftViewHolder = (GiftViewHolder) convertView.getTag();
        }

        if(groupPosition == 0)
            lotteryGiftModel = new LotteryModel.LotteryGiftModel(lotteryModel.getGiftFirstName(), lotteryModel.getGiftFirstImg(), lotteryModel.getGiftFirstResult());
        else if(groupPosition == 1)
            lotteryGiftModel = new LotteryModel.LotteryGiftModel(lotteryModel.getGiftSecondName(), lotteryModel.getGiftSecondImg(), lotteryModel.getGiftSecondResult());
        else
            lotteryGiftModel = new LotteryModel.LotteryGiftModel(lotteryModel.getGiftThirdName(), lotteryModel.getGiftThirdImg(), lotteryModel.getGiftThirdResult());
        giftViewHolder.ranking.setText(ctx.getResources().getStringArray(R.array.lottery_ranking)[groupPosition]);

        giftViewHolder.gift.setText(lotteryGiftModel.getGiftName());
        giftViewHolder.expand.setVisibility(View.VISIBLE);
        if(lotteryModel.getStatus() != 0 && isExpanded)
            giftViewHolder.expand.setRotation(180);
        else if(lotteryModel.getStatus() != 0)
            giftViewHolder.expand.setRotation(0);
        else
            giftViewHolder.expand.setVisibility(View.GONE);

        if(lotteryGiftModel.getGiftImg() != null && !lotteryGiftModel.getGiftImg().equals(""))
        {
            giftViewHolder.card.setOnClickListener(onImgClick(lotteryGiftModel.getGiftImg()));
            giftViewHolder.img.setTag(lotteryGiftModel.getGiftImg());
            BitmapDrawable c = setImageFormWeb(lotteryGiftModel.getGiftImg());
            if(c != null) giftViewHolder.img.setImageDrawable(c);
            giftViewHolder.img.setPadding(0, 0, 0, 0);
        }
        else
        {
            giftViewHolder.card.setOnClickListener(null);
            giftViewHolder.img.setImageResource(R.drawable.icon_lottery);
            giftViewHolder.img.setPadding(4, 4, 4, 4);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        ArrayList<LotteryModel.LotteryUserModel> lotteryUserModelArrayList;
        UserViewHolder userViewHolder;
        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.item_lottery_gift_user, parent, false);
            userViewHolder = new UserViewHolder();
            userViewHolder.lay = convertView.findViewById(R.id.item_lottery_user);
            userViewHolder.img = convertView.findViewById(R.id.item_lottery_user_img);
            userViewHolder.name = convertView.findViewById(R.id.item_lottery_user_name);
            convertView.setTag(userViewHolder);
        }
        else
        {
            userViewHolder = (UserViewHolder) convertView.getTag();
        }

        if(groupPosition == 0)
            lotteryUserModelArrayList = lotteryModel.getGiftFirstResult();
        else if(groupPosition == 1)
            lotteryUserModelArrayList = lotteryModel.getGiftSecondResult();
        else
            lotteryUserModelArrayList = lotteryModel.getGiftThirdResult();

        userViewHolder.lay.setOnClickListener(onUserClick(lotteryUserModelArrayList.get(childPosition).getUid()));
        userViewHolder.name.setText(lotteryUserModelArrayList.get(childPosition).getName());
        userViewHolder.img.setImageResource(R.drawable.img_default_avatar);

        userViewHolder.img.setTag(lotteryUserModelArrayList.get(childPosition).getImg());
        BitmapDrawable c = setImageFormWeb(lotteryUserModelArrayList.get(childPosition).getImg());
        if(c != null) userViewHolder.img.setImageDrawable(c);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return true;
    }

    private static class GiftViewHolder
    {
        CardView card;
        ImageView img;
        TextView ranking;
        TextView gift;
        ImageView expand;
    }

    private static class UserViewHolder
    {
        RelativeLayout lay;
        RoundedImageView img;
        TextView name;
    }

    private BitmapDrawable setImageFormWeb(String url)
    {
        if(url != null && LruCacheUtil.getLruCache().get(url) != null)
            return LruCacheUtil.getLruCache().get(url);
        else
        {
            ImageTaskUtil it = new ImageTaskUtil(listView);
            it.execute(url);
            return null;
        }
    }

    private int existGift(String name)
    {
        return (name != null && !name.equals("")) ? 1 : 0;
    }

    private View.OnClickListener onImgClick(String url)
    {
        return v -> lotteryListener.onImgClick(url);
    }

    private View.OnClickListener onUserClick(String uid)
    {
        return v -> lotteryListener.onUserClick(uid);
    }

    public interface LotteryListener
    {
        void onImgClick(String url);
        void onUserClick(String uid);
    }
}
