import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { fetchUser, API } from "../api/api";
import Cropper from "react-easy-crop";

export default function EditProfilePage() {
    const { userId } = useParams();
    const navigate = useNavigate();

    const [username, setUsername] = useState("");
    const [bio, setBio] = useState("");

    const [currentProfilePicture, setCurrentProfilePicture] = useState<string | null>(null);
    const [profileFile, setProfileFile] = useState<File | null>(null);

    // Cropper state
    const [imageSrc, setImageSrc] = useState<string | null>(null);
    const [crop, setCrop] = useState({ x: 0, y: 0 });
    const [zoom, setZoom] = useState(1);
    const [croppedAreaPixels, setCroppedAreaPixels] = useState<any>(null);
    const [fileType, setFileType] = useState<"image/png" | "image/jpeg">("image/jpeg");

    useEffect(() => {
        if (!userId) return;

        const loadUser = async () => {
            try {
                const userData = await fetchUser(userId);
                setUsername(userData.username);
                setBio(userData.bio);

                if (userData.profilePicture) {
                    setCurrentProfilePicture(`${import.meta.env.VITE_API_URL}${userData.profilePicture}`);
                }
            } catch (err) {
                console.error(err);
            }
        };

        loadUser();

    }, [userId]);

    const onCropComplete = useCallback((_: any, croppedAreaPixels: any) => {
        setCroppedAreaPixels(croppedAreaPixels);
    }, []);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            const file = e.target.files[0];
            setFileType(file.type === "image/png" ? "image/png" : "image/jpeg");

            const reader = new FileReader();
            reader.onload = () => setImageSrc(reader.result as string);
            reader.readAsDataURL(file);
        }
    };

    const getCroppedImg = (imageSrc: string, crop: any, type: string): Promise<Blob> => {
        return new Promise((resolve, reject) => {
            const image = new Image();
            image.src = imageSrc;
            image.onload = () => {
                const canvas = document.createElement("canvas");
                canvas.width = crop.width;
                canvas.height = crop.height;
                const ctx = canvas.getContext("2d");
                if (!ctx) return reject(new Error("Failed to get canvas context"));

                // preserve transparency for PNG
                if (type === "image/png") ctx.clearRect(0, 0, canvas.width, canvas.height);

                ctx.drawImage(
                    image,
                    crop.x,
                    crop.y,
                    crop.width,
                    crop.height,
                    0,
                    0,
                    crop.width,
                    crop.height
                );

                canvas.toBlob((blob) => {
                    if (!blob) return reject(new Error("Canvas is empty"));
                    resolve(blob);
                }, type);
            };
            image.onerror = reject;
        });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        let fileToUpload = profileFile;

        if (imageSrc && croppedAreaPixels) {
            const croppedBlob = await getCroppedImg(imageSrc, croppedAreaPixels, fileType);
            fileToUpload = new File([croppedBlob], `profile.${fileType === "image/png" ? "png" : "jpg"}`, { type: fileType });
        }

        const formData = new FormData();
        formData.append("user", new Blob([JSON.stringify({ bio })], { type: "application/json" }));
        if (fileToUpload) formData.append("file", fileToUpload);

        await API.put(`/api/v1/users/${userId}`, formData, {
            headers: { "Content-Type": "multipart/form-data" },
        });

        navigate(`/profile/${userId}`);
    };

    return (
        <div className="flex flex-col items-center w-full min-h-screen pb-12">
            {/* Scrollable content */}
            <form onSubmit={handleSubmit} className="p-4 flex flex-col gap-4 items-center w-full max-w-xl pb-24">
                <div className="relative w-64 h-64 rounded-full overflow-hidden bg-gray-200">
                    {imageSrc ? (
                        <Cropper
                            image={imageSrc}
                            crop={crop}
                            zoom={zoom}
                            aspect={1}
                            cropShape="round"
                            showGrid={false}
                            onCropChange={setCrop}
                            onZoomChange={setZoom}
                            onCropComplete={onCropComplete}
                        />
                    ) : (
                        <img
                            src={currentProfilePicture || "/default_profile_SocialEX.png"}
                            alt="Profile"
                            className="w-full h-full object-cover"
                        />
                    )}
                </div>

                {/* Zoom slider below the circle */}
                {imageSrc && (
                    <input
                        type="range"
                        min={1}
                        max={5}
                        step={0.01}
                        value={zoom}
                        onChange={(e) => setZoom(Number(e.target.value))}
                        className="w-64 mt-4"
                    />
                )}

                <input type="file" accept="image/*" onChange={handleFileChange} />

                <input
                    type="text"
                    value={username}
                    readOnly
                    className="border p-2 rounded bg-gray-100 cursor-not-allowed w-64"
                />

                <textarea
                    value={bio}
                    onChange={(e) => setBio(e.target.value)}
                    className="border p-2 rounded w-64"
                    placeholder="Bio"
                />

                <button
                    type="submit"
                    className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg w-64"
                >
                    Save Changes
                </button>
            </form>

        </div>
    );
}
