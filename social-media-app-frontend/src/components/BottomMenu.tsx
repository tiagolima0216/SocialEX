// BottomMenu.tsx
import { Link, useLocation } from "react-router-dom";
import { FaHome, FaEnvelope, FaUser } from "react-icons/fa";

export default function BottomMenu({ userId }: { userId: string }) {
    const location = useLocation();

    const getClass = (path: string) =>
        location.pathname === path
            ? "text-blue-500"
            : "text-gray-400 hover:text-blue-500";

    return (
        <div className="fixed bottom-0 left-0 right-0 bg-white border-t p-2 flex justify-around items-center z-20">
            <Link to="/messages" className={getClass("/messages")}>
                <FaEnvelope size={24} />
            </Link>
            <Link to="/" className={getClass("/")}>
                <FaHome size={28} />
            </Link>
            <Link to={`/profile/${userId}`} className={getClass(`/profile/${userId}`)}>
                <FaUser size={24} />
            </Link>
        </div>
    );
}
