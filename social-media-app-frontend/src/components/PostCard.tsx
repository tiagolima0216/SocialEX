import type { Post } from "../models/Post";

interface PostCardProps {
    post: Post;
}

export default function PostCard({ post }: PostCardProps) {
    return (
        <div className="bg-white p-4 rounded-2xl shadow mb-4">
            <div className="flex items-center gap-3 mb-2">
                <img
                    src={post.author?.profilePicture || "/default-avatar.png"}
                    alt={post.author?.username || "Unknown user"}
                    className="w-10 h-10 rounded-full"
                />
                <div>
                    <p className="font-semibold">{post.author?.username || "Anonymous"}</p>
                    <p className="text-xs text-gray-500">
                        {post.createdAt ? new Date(post.createdAt).toLocaleString() : ""}
                    </p>
                </div>
            </div>
            <p className="text-gray-800">{post.content}</p>
            <div className="flex gap-4 mt-3 text-gray-500 text-sm">
                <span>❤️ {post.likes}</span>
                <span>💬 {post.commentsCount}</span>
            </div>
        </div>
    );
}