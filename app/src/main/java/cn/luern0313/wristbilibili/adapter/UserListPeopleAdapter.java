package cn.luern0313.wristbilibili.adapter;

import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.UserListPeopleModel;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 被 luern0313 创建于 2020/4/23.
 */
public class UserListPeopleAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;

    private UserListPeopleAdapterListener userListPeopleAdapterListener;
    private int mode;

    private ArrayList<UserListPeopleModel> userListPeopleModelArrayList;
    private ListView listView;

    public UserListPeopleAdapter(LayoutInflater inflater, ArrayList<UserListPeopleModel> userListPeopleModelArrayList, int mode, ListView listView, UserListPeopleAdapterListener userListPeopleAdapterListener)
    {
        mInflater = inflater;
        this.userListPeopleModelArrayList = userListPeopleModelArrayList;
        this.mode = mode;
        this.listView = listView;
        this.userListPeopleAdapterListener = userListPeopleAdapterListener;
    }

    @Override
    public int getCount()
    {
        return userListPeopleModelArrayList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return position;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup)
    {
        UserListPeopleModel userListPeopleModel = userListPeopleModelArrayList.get(position);
        ViewHolder viewHolder;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_user_list_people, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);

            viewHolder.lay = convertView.findViewById(R.id.user_list_people_lay);
            viewHolder.img = convertView.findViewById(R.id.user_list_people_face);
            viewHolder.name = convertView.findViewById(R.id.user_list_people_name);
            viewHolder.sign = convertView.findViewById(R.id.user_list_people_sign);
            viewHolder.time = convertView.findViewById(R.id.user_list_people_time);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.img.setImageResource(R.drawable.img_default_head);
        viewHolder.name.setText(userListPeopleModel.getName());
        if(userListPeopleModel.getVip() == 2)
            viewHolder.name.setTextColor(listView.getResources().getColor(R.color.mainColor));
        else
            viewHolder.name.setTextColor(listView.getResources().getColor(R.color.gray_44));
        String sign = userListPeopleModel.getVerifyName().equals("") ?
                userListPeopleModel.getSign() : userListPeopleModel.getVerifyName();
        if(!sign.equals(""))
        {
            viewHolder.sign.setVisibility(View.VISIBLE);
            viewHolder.sign.setText(sign);
        }
        else
            viewHolder.sign.setVisibility(View.GONE);
        viewHolder.time.setText((mode == 2 ? "关注于：" : "粉于：") + userListPeopleModel.getMtime());
        switch (userListPeopleModel.getVerifyType())
        {
            case "0":
                convertView.findViewById(R.id.user_list_people_official_1).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.user_list_people_official_2).setVisibility(View.GONE);
                break;
            case "1":
                convertView.findViewById(R.id.user_list_people_official_1).setVisibility(View.GONE);
                convertView.findViewById(R.id.user_list_people_official_2).setVisibility(View.VISIBLE);
                break;
            default:
                convertView.findViewById(R.id.user_list_people_official_1).setVisibility(View.GONE);
                convertView.findViewById(R.id.user_list_people_official_2).setVisibility(View.GONE);
                break;
        }
        viewHolder.img.setTag(userListPeopleModel.getFace());
        BitmapDrawable f = setImageFormWeb(userListPeopleModel.getFace());
        if(f != null) viewHolder.img.setImageDrawable(f);

        viewHolder.lay.setOnClickListener(onViewClick(position));

        return convertView;
    }

    class ViewHolder
    {
        RelativeLayout lay;
        CircleImageView img;
        TextView name;
        TextView sign;
        TextView time;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                userListPeopleAdapterListener.onUserListPeopleAdapterClick(v.getId(), position);
            }
        };
    }

    private BitmapDrawable setImageFormWeb(String url)
    {
        if(LruCacheUtil.getLruCache().get(url) != null)
        {
            return LruCacheUtil.getLruCache().get(url);
        }
        else
        {
            ImageTaskUtil it = new ImageTaskUtil(listView);
            it.execute(url);
            return null;
        }
    }

    public interface UserListPeopleAdapterListener
    {
        void onUserListPeopleAdapterClick(int viewId, int position);
    }
}
