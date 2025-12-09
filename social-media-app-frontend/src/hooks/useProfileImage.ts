import { useState, useEffect } from "react";
import { API } from "../api/api";

const useProfileImage = (userId: string) => {
    const [imageSrc, setImageSrc] = useState("/default_profile_SocialEX.png");

    useEffect(() => {
        const fetchImage = async () => {
            try {
                const res = await API.get(`/api/v1/users/${userId}/profile-picture`, { responseType: "blob" });
                const url = URL.createObjectURL(res.data);
                setImageSrc(url);
            } catch {
                setImageSrc("/default_profile_SocialEX.png");
            }
        };
        fetchImage();
    }, [userId]);

    return imageSrc;
};

export default useProfileImage;
